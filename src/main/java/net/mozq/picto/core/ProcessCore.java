/*!
 * Picto
 * Copyright 2016 Mozq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mozq.picto.core;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.imaging.common.ImageMetadata;

import net.mozq.nanotemplate.NanoTemplateException;
import net.mozq.picto.App;
import net.mozq.picto.core.exception.PictoException;
import net.mozq.picto.core.exception.PictoInvalidDestinationPathException;
import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.OperationType;
import net.mozq.picto.enums.ProcessDataStatus;
import net.mozq.picto.view.Messages;

public final class ProcessCore {

	private static final CopyOption[] OPTIONS_COPY = {
			//StandardCopyOption.COPY_ATTRIBUTES,
	};

	private static final CopyOption[] OPTIONS_COPY_REPLACE = {
			//StandardCopyOption.COPY_ATTRIBUTES,
			StandardCopyOption.REPLACE_EXISTING,
	};

	private static final CopyOption[] OPTIONS_MOVE = {
			//StandardCopyOption.ATOMIC_MOVE,
	};

	private static final CopyOption[] OPTIONS_MOVE_REPLACE = {
			//StandardCopyOption.ATOMIC_MOVE,
			StandardCopyOption.REPLACE_EXISTING,
	};

	private ProcessCore() {
	}

	public record CachedFile(Path path, BasicFileAttributes attrs) {
	}

	public static void findFiles(
			ProcessCondition processCondition,
			Consumer<ProcessData> processDataSetter,
			BooleanSupplier processStopper
			) throws IOException {

		Set<FileVisitOption> fileVisitOptionSet;
		if (processCondition.isFollowLinks()) {
			fileVisitOptionSet = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		} else {
			fileVisitOptionSet = Collections.emptySet();
		}

		Files.walkFileTree(
				processCondition.getSrcFolder(),
				fileVisitOptionSet,
				processCondition.getDepth(),
				new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						if (processStopper.getAsBoolean()) {
							return FileVisitResult.TERMINATE;
						}

						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

						if (attrs.isDirectory()) {
							return FileVisitResult.SKIP_SUBTREE;
						}

						if (processStopper.getAsBoolean()) {
							return FileVisitResult.TERMINATE;
						}

						if (!processCondition.getPathFilter().accept(file, attrs)) {
							return FileVisitResult.SKIP_SUBTREE;
						}

						Path rootRelativeSubPath = processCondition.getSrcFolder().relativize(file.getParent());

						Supplier<ImageMetadata> imageMetadataSupplier = memoize(() -> ExifMetadataSupport.loadMetadata(file));

						Instant baseDate;
						if (processCondition.isChangeFileCreationDate()
								|| processCondition.isChangeFileModifiedDate()
								|| processCondition.isChangeFileAccessDate()
								|| processCondition.isChangeFileExifDate()
								) {
							baseDate = getBaseDate(processCondition, file, attrs, imageMetadataSupplier);
						} else {
							baseDate = null;
						}

						String destSubPathname;
						try {
							TemplateVariables templateVariables = new TemplateVariables(
									processCondition,
									file,
									attrs,
									rootRelativeSubPath,
									imageMetadataSupplier,
									baseDate);
							destSubPathname = processCondition.getDestSubFilePathTemplate().render(varName -> {
								try {
									return templateVariables.resolve(varName);
								} catch (PictoException e) {
									throw e;
								} catch (Exception e) {
									throw new PictoInvalidDestinationPathException(
											Messages.getString("message.warn.invalid.destSubFilePath.pattern"),
											e
											);
								}
							});
						} catch (NanoTemplateException e) {
							Throwable cause = e.getCause();
							if (cause instanceof PictoException) {
								throw (PictoException)cause;
							}
							throw new PictoInvalidDestinationPathException(
									Messages.getString("message.warn.invalid.destSubFilePath.pattern"),
									e
									);
						}

						// A blank leading path segment (e.g. an empty ${SubFolderPath} joined with "/")
						// would otherwise resolve to an OS-absolute path and escape the destination folder.
						String normalizedDestSubPathname = destSubPathname.replaceFirst("^[/\\\\]+", "");

						if (normalizedDestSubPathname.isBlank()) {
							ProcessData processData = new ProcessData();
							processData.setSrcPath(file);
							processData.setSrcFileAttributes(attrs);
							processData.setSrcRelativePath(processCondition.getSrcFolder().relativize(file).toString());
							processData.setDestRelativePath("");
							processData.setStatus(ProcessDataStatus.Error);
							processData.setMessage(Messages.getString("message.warn.destSubFilePath.empty"));

							processDataSetter.accept(processData);

							return FileVisitResult.CONTINUE;
						}

						Path destSubPath = processCondition.getDestFolder().resolve(normalizedDestSubPathname).normalize();

						if (!destSubPath.startsWith(processCondition.getDestFolder())) {
							throw new PictoInvalidDestinationPathException(
									Messages.getString("message.warn.invalid.destination.path", destSubPath)
									);
						}

						ProcessData processData = new ProcessData();
						processData.setSrcPath(file);
						processData.setSrcFileAttributes(attrs);
						processData.setDestPath(destSubPath);
						processData.setSrcRelativePath(processCondition.getSrcFolder().relativize(file).toString());
						processData.setDestRelativePath(processCondition.getDestFolder().relativize(destSubPath).toString());
						processData.setBaseDate(baseDate);

						processDataSetter.accept(processData);

						return FileVisitResult.CONTINUE;
					}
				}
				);
	}

	public static void processFiles(
			ProcessCondition processCondition,
			Supplier<ProcessData> processDataGetter,
			BiConsumer<Integer, ProcessData> processDataUpdater,
			Function<ProcessData, ProcessDataStatus> overwriteConfirm,
			BooleanSupplier processStopper
			) throws IOException {

		int index = 0;
		while (!processStopper.getAsBoolean()) {

			ProcessData processData = processDataGetter.get();
			if (processData == null) {
				break;
			}

			if (processData.getStatus() != ProcessDataStatus.Error) {
				processData.setStatus(ProcessDataStatus.Processing);
				processDataUpdater.accept(index, processData);

				ProcessDataStatus status;
				try {
					if (processCondition.isDryRun()) {
						// NOP
						status = ProcessDataStatus.Success;
					} else {
						status = process(processCondition, processData, overwriteConfirm);
					}
					processData.setStatus(status);
				} catch (Exception e) {
					processData.setStatus(ProcessDataStatus.Error);
					processData.setMessage(e.getLocalizedMessage());
					App.handleWarn(e.getMessage(), e);
				}
			}

			processDataUpdater.accept(index, processData);
			if (processData.getStatus() == ProcessDataStatus.Error
					|| processData.getStatus() == ProcessDataStatus.Terminated) {
				break;
			}

			index++;
		}
	}

	private static ProcessDataStatus confirmOverwrite(
			ProcessCondition processCondition,
			ProcessData processData,
			Function<ProcessData, ProcessDataStatus> overwriteConfirm
			) {

		return switch (processCondition.getExistingFileMethod()) {
		case Skip -> ProcessDataStatus.Skipped;
		case Confirm -> overwriteConfirm.apply(processData);
		case Terminate -> ProcessDataStatus.Terminated;
		case Overwrite -> ProcessDataStatus.Processing;
		};
	}

	private static ProcessDataStatus process(
			ProcessCondition processCondition,
			ProcessData processData,
			Function<ProcessData, ProcessDataStatus> overwriteConfirm
			) throws IOException {

		ProcessDataStatus status;
		Path outputPath = outputPath(processCondition, processData);

		Path outputParentPath = outputPath.getParent();
		if (outputParentPath != null) {
			Files.createDirectories(outputParentPath);
		}

		if (processCondition.isCheckFileDigest()
				|| (processCondition.isChangeFileExifDate() && processData.getBaseDate() != null)
				|| processCondition.isRemoveExifGps()
				|| processCondition.isRemoveExifAll()
				) {
			Path destTempPath = null;
			try {
				destTempPath = createTempFile(outputPath);

				if (processCondition.isCheckFileDigest()) {
					FileDigestSupport.copyAndVerify(processData.getSrcPath(), destTempPath);
				} else if (processCondition.isRemoveExifAll()) {
					ExifMetadataSupport.removeAll(processData.getSrcPath(), destTempPath);
				} else if (processCondition.isChangeFileExifDate() || processCondition.isRemoveExifGps()) {
					Instant exifDate = processCondition.isChangeFileExifDate() ? processData.getBaseDate() : null;
					ExifMetadataSupport.updateLossless(
							processData.getSrcPath(),
							destTempPath,
							exifDate,
							processCondition.getTimeZone(),
							processCondition.isRemoveExifGps());
				}

				try {
					if (processCondition.getOperationType() == OperationType.Overwrite) {
						Files.move(destTempPath, outputPath, OPTIONS_MOVE_REPLACE);
					} else {
						Files.move(destTempPath, outputPath, OPTIONS_MOVE);
					}
					if (processCondition.getOperationType() == OperationType.Move) {
						Files.deleteIfExists(processData.getSrcPath());
					}
					status = ProcessDataStatus.Success;
				} catch (FileAlreadyExistsException _) {
					status = confirmOverwrite(processCondition, processData, overwriteConfirm);
					if (status == ProcessDataStatus.Processing) {
						// Overwrite
						Files.move(destTempPath, outputPath, OPTIONS_MOVE_REPLACE);
						if (processCondition.getOperationType() == OperationType.Move) {
							Files.deleteIfExists(processData.getSrcPath());
						}
						status = ProcessDataStatus.Success;
					}
				}
			} finally {
				if (destTempPath != null) {
					Files.deleteIfExists(destTempPath);
				}
			}
		} else {
			status = switch (processCondition.getOperationType()) {
			case Copy -> {
				try {
					Files.copy(processData.getSrcPath(), outputPath, OPTIONS_COPY);
					yield ProcessDataStatus.Success;
				} catch (FileAlreadyExistsException _) {
					ProcessDataStatus overwriteStatus = confirmOverwrite(processCondition, processData, overwriteConfirm);
					if (overwriteStatus == ProcessDataStatus.Processing) {
						Files.copy(processData.getSrcPath(), outputPath, OPTIONS_COPY_REPLACE);
						yield ProcessDataStatus.Success;
					}
					yield overwriteStatus;
				}
			}
			case Move -> {
				try {
					Files.move(processData.getSrcPath(), outputPath, OPTIONS_MOVE);
					yield ProcessDataStatus.Success;
				} catch (FileAlreadyExistsException _) {
					ProcessDataStatus overwriteStatus = confirmOverwrite(processCondition, processData, overwriteConfirm);
					if (overwriteStatus == ProcessDataStatus.Processing) {
						Files.move(processData.getSrcPath(), outputPath, OPTIONS_MOVE_REPLACE);
						yield ProcessDataStatus.Success;
					}
					yield overwriteStatus;
				}
			}
			case Overwrite -> ProcessDataStatus.Success;
			};
		}

		if (status == ProcessDataStatus.Success) {
			FileTime creationFileTime = processData.getSrcFileAttributes().creationTime();
			FileTime modifiedFileTime = processData.getSrcFileAttributes().lastModifiedTime();
			FileTime accessFileTime = processData.getSrcFileAttributes().lastAccessTime();
			if (processCondition.isChangeFileCreationDate()
					|| processCondition.isChangeFileModifiedDate()
					|| processCondition.isChangeFileAccessDate()
					) {
				if (processData.getBaseDate() != null) {
					FileTime baseFileTime = FileTime.from(processData.getBaseDate());
					if (processCondition.isChangeFileCreationDate()) {
						creationFileTime = baseFileTime;
					}
					if (processCondition.isChangeFileModifiedDate()) {
						modifiedFileTime = baseFileTime;
					}
					if (processCondition.isChangeFileAccessDate()) {
						accessFileTime = baseFileTime;
					}
				}
			}
			BasicFileAttributeView attributeView = Files.getFileAttributeView(outputPath, BasicFileAttributeView.class);
			attributeView.setTimes(modifiedFileTime, accessFileTime, creationFileTime);
		}

		return status;
	}

	private static Path outputPath(ProcessCondition processCondition, ProcessData processData) {
		return processCondition.getOperationType() == OperationType.Overwrite
				? processData.getSrcPath()
				: processData.getDestPath();
	}

	private static Path createTempFile(Path outputPath) throws IOException {
		Path parentPath = outputPath.getParent();
		String prefix = outputPath.getFileName().toString();
		return parentPath != null
				? Files.createTempFile(parentPath, prefix, null)
				: Files.createTempFile(prefix, null);
	}

	private static Instant getBaseDate(
			ProcessCondition processCondition,
			Path file, BasicFileAttributes attrs,
			Supplier<ImageMetadata> imageMetadataSupplier
			) throws IOException {

		Instant baseDate = switch (processCondition.getBaseDateType()) {
		case CurrentDate -> Instant.now();
		case FileCreationDate -> attrs.creationTime().toInstant();
		case FileModifiedDate -> attrs.lastModifiedTime().toInstant();
		case FileAccessDate -> attrs.lastAccessTime().toInstant();
		case ExifDate -> ExifMetadataSupport.exifDate(imageMetadataSupplier.get());
		case CustomDate -> processCondition.getCustomBaseDate();
		};

		if (baseDate != null) {
			if (processCondition.getAdjustmentType() != DateModType.None) {
				ZonedDateTime zdt = baseDate.atZone(processCondition.getTimeZone().toZoneId());
				switch (processCondition.getAdjustmentType()) {
				case None -> {}
				case Minus, Plus -> {
					int signum = processCondition.getAdjustmentType() == DateModType.Minus ? -1 : 1;
					zdt = plusField(zdt, processCondition.getAdjustmentYears(), signum, ChronoUnit.YEARS);
					zdt = plusField(zdt, processCondition.getAdjustmentMonths(), signum, ChronoUnit.MONTHS);
					zdt = plusField(zdt, processCondition.getAdjustmentDays(), signum, ChronoUnit.DAYS);
					zdt = plusField(zdt, processCondition.getAdjustmentHours(), signum, ChronoUnit.HOURS);
					zdt = plusField(zdt, processCondition.getAdjustmentMinutes(), signum, ChronoUnit.MINUTES);
					zdt = plusField(zdt, processCondition.getAdjustmentSeconds(), signum, ChronoUnit.SECONDS);
				}
				case Overwrite -> {
					// Each field is set by resetting to that field's own minimum (month 1, day 1, hour/minute/
					// second 0) and adding the user's value as an offset from there, so an out-of-range value
					// rolls into the next larger field instead of being rejected - matching Calendar.set's old
					// lenient behavior (e.g. month 13 becomes January of the following year) with no risk of
					// ZonedDateTime.with* throwing for an out-of-range field.
					zdt = withField(zdt, processCondition.getAdjustmentYears(), (z, y) -> z.withYear(clamp(y, Year.MIN_VALUE, Year.MAX_VALUE)));
					zdt = withField(zdt, processCondition.getAdjustmentMonths(), (z, m) -> z.withMonth(1).plusMonths(m - 1));
					zdt = withField(zdt, processCondition.getAdjustmentDays(), (z, d) -> z.withDayOfMonth(1).plusDays(d - 1));
					zdt = withField(zdt, processCondition.getAdjustmentHours(), (z, h) -> z.withHour(0).plusHours(h));
					zdt = withField(zdt, processCondition.getAdjustmentMinutes(), (z, m) -> z.withMinute(0).plusMinutes(m));
					zdt = withField(zdt, processCondition.getAdjustmentSeconds(), (z, s) -> z.withSecond(0).plusSeconds(s));
				}
				}
				baseDate = zdt.toInstant();
			}
		}

		return baseDate;
	}

	private static <T> Supplier<T> memoize(Supplier<T> supplier) {
		return new Supplier<>() {
			private boolean loaded;
			private T value;

			@Override
			public T get() {
				if (!loaded) {
					value = supplier.get();
					loaded = true;
				}
				return value;
			}
		};
	}

	private static ZonedDateTime plusField(ZonedDateTime zdt, Integer amount, int signum, ChronoUnit unit) {
		return amount == null ? zdt : zdt.plus((long)signum * amount.intValue(), unit);
	}

	private static ZonedDateTime withField(ZonedDateTime zdt, Integer amount, BiFunction<ZonedDateTime, Integer, ZonedDateTime> setter) {
		return amount == null ? zdt : setter.apply(zdt, amount);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

}
