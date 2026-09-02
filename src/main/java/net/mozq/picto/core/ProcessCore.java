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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
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
				processCondition.getSrcRootPath(),
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

						Path rootRelativeSubPath = processCondition.getSrcRootPath().relativize(file.getParent());

						Supplier<ImageMetadata> imageMetadataSupplier = memoize(() -> ExifMetadataSupport.loadMetadata(file));

						Date baseDate;
						if (processCondition.isChangeFileCreationDate()
								|| processCondition.isChangeFileModifiedDate()
								|| processCondition.isChangeFileAccessDate()
								|| processCondition.isChangeExifDate()
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
							destSubPathname = processCondition.getDestSubPathTemplate().render(varName -> {
								try {
									return templateVariables.resolve(varName);
								} catch (PictoException e) {
									throw e;
								} catch (Exception e) {
									throw new PictoInvalidDestinationPathException(
											Messages.getString("message.warn.invalid.destSubPath.pattern"),
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
									Messages.getString("message.warn.invalid.destSubPath.pattern"),
									e
									);
						}

						Path destSubPath = processCondition.getDestRootPath().resolve(destSubPathname).normalize();

						if (!destSubPath.startsWith(processCondition.getDestRootPath())) {
							throw new PictoInvalidDestinationPathException(
									Messages.getString("message.warn.invalid.destination.path", destSubPath)
									);
						}

						ProcessData processData = new ProcessData();
						processData.setSrcPath(file);
						processData.setSrcFileAttributes(attrs);
						processData.setDestPath(destSubPath);
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

		switch (processCondition.getExistingFileMethod()) {
		case Skip:
			return ProcessDataStatus.Skipped;
		case Confirm:
			return overwriteConfirm.apply(processData);
		case Terminate:
			return ProcessDataStatus.Terminated;
		case Overwrite:
			return ProcessDataStatus.Processing;
		default:
			throw new IllegalStateException(processCondition.getExistingFileMethod().toString());
		}
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

		if (processCondition.isCheckDigest()
				|| (processCondition.isChangeExifDate() && processData.getBaseDate() != null)
				|| processCondition.isRemoveExifTagsGps()
				|| processCondition.isRemoveExifTagsAll()
				) {
			Path destTempPath = null;
			try {
				destTempPath = createTempFile(outputPath);

				if (processCondition.isCheckDigest()) {
					FileDigestSupport.copyAndVerify(processData.getSrcPath(), destTempPath);
				} else if (processCondition.isRemoveExifTagsAll()) {
					ExifMetadataSupport.removeAll(processData.getSrcPath(), destTempPath);
				} else if (processCondition.isChangeExifDate() || processCondition.isRemoveExifTagsGps()) {
					Date exifDate = processCondition.isChangeExifDate() ? processData.getBaseDate() : null;
					ExifMetadataSupport.updateLossless(
							processData.getSrcPath(),
							destTempPath,
							exifDate,
							processCondition.getTimeZone(),
							processCondition.isRemoveExifTagsGps());
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
				} catch (FileAlreadyExistsException e) {
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
			switch (processCondition.getOperationType()) {
			case Copy:
				try {
					Files.copy(processData.getSrcPath(), outputPath, OPTIONS_COPY);
					status = ProcessDataStatus.Success;
				} catch (FileAlreadyExistsException e) {
					status = confirmOverwrite(processCondition, processData, overwriteConfirm);
					if (status == ProcessDataStatus.Processing) {
						Files.copy(processData.getSrcPath(), outputPath, OPTIONS_COPY_REPLACE);
						status = ProcessDataStatus.Success;
					}
				}
				break;
			case Move:
				try {
					Files.move(processData.getSrcPath(), outputPath, OPTIONS_MOVE);
					status = ProcessDataStatus.Success;
				} catch (FileAlreadyExistsException e) {
					status = confirmOverwrite(processCondition, processData, overwriteConfirm);
					if (status == ProcessDataStatus.Processing) {
						Files.move(processData.getSrcPath(), outputPath, OPTIONS_MOVE_REPLACE);
						status = ProcessDataStatus.Success;
					}
				}
				break;
			case Overwrite:
				// NOP
				status = ProcessDataStatus.Success;
				break;
			default:
				throw new IllegalStateException(processCondition.getOperationType().toString());
			}
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
					FileTime baseFileTime = FileTime.fromMillis(processData.getBaseDate().getTime());
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

	private static Date getBaseDate(
			ProcessCondition processCondition,
			Path file, BasicFileAttributes attrs,
			Supplier<ImageMetadata> imageMetadataSupplier
			) throws IOException {

		Date baseDate = null;
		switch (processCondition.getBaseDateType()) {
		case CurrentDate:
			baseDate = new Date(System.currentTimeMillis());
			break;
		case FileCreationDate:
			baseDate = toDate(attrs.creationTime());
			break;
		case FileModifiedDate:
			baseDate = toDate(attrs.lastModifiedTime());
			break;
		case FileAccessDate:
			baseDate = toDate(attrs.lastAccessTime());
			break;
		case ExifDate:
			baseDate = ExifMetadataSupport.exifDate(imageMetadataSupplier.get());
			break;
		case CustomDate:
			baseDate = processCondition.getCustomBaseDate();
			break;
		default:
			throw new IllegalStateException(processCondition.getBaseDateType().toString());
		}

		if (baseDate != null) {
			if (processCondition.getBaseDateModType() != DateModType.None) {
				Calendar cal = Calendar.getInstance(processCondition.getTimeZone());
				cal.setTime(baseDate);
				int signum = 1;
				switch (processCondition.getBaseDateModType()) {
				case None:
					break;
				case Minus:
					signum = -1;
					// FALLTHRU
				case Plus:
					addField(cal, Calendar.YEAR, processCondition.getBaseDateModYears(), signum);
					addField(cal, Calendar.MONTH, processCondition.getBaseDateModMonths(), signum);
					addField(cal, Calendar.DAY_OF_MONTH, processCondition.getBaseDateModDays(), signum);
					addField(cal, Calendar.HOUR_OF_DAY, processCondition.getBaseDateModHours(), signum);
					addField(cal, Calendar.MINUTE, processCondition.getBaseDateModMinutes(), signum);
					addField(cal, Calendar.SECOND, processCondition.getBaseDateModSeconds(), signum);
					break;
				case Overwrite:
					setField(cal, Calendar.YEAR, processCondition.getBaseDateModYears());
					setField(cal, Calendar.MONTH, processCondition.getBaseDateModMonths());
					setField(cal, Calendar.DAY_OF_MONTH, processCondition.getBaseDateModDays());
					setField(cal, Calendar.HOUR_OF_DAY, processCondition.getBaseDateModHours());
					setField(cal, Calendar.MINUTE, processCondition.getBaseDateModMinutes());
					setField(cal, Calendar.SECOND, processCondition.getBaseDateModSeconds());
					break;
				default:
					throw new IllegalStateException(processCondition.getBaseDateModType().toString());
				}
				baseDate = cal.getTime();
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

	private static Date toDate(FileTime fileTime) {
		if (fileTime == null) {
			return null;
		}
		return new Date(fileTime.toMillis());
	}

	private static boolean addField(Calendar cal, int field, Integer amount, int signum) {
		if (amount == null) {
			return false;
		}

		cal.add(field, signum * amount.intValue());
		return true;
	}

	private static boolean setField(Calendar cal, int field, Integer amount) {
		if (amount == null) {
			return false;
		}

		cal.set(field, amount.intValue());
		return true;
	}

}
