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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata.GPSInfo;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.mifmi.commons4j.io.file.FileUtilz;

import net.mozq.picto.App;
import net.mozq.picto.core.exception.PictoException;
import net.mozq.picto.core.exception.PictoFileChangeException;
import net.mozq.picto.core.exception.PictoFileDigestMismatchException;
import net.mozq.picto.core.exception.PictoInvalidDestinationPathException;
import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.OperationType;
import net.mozq.picto.enums.ProcessDataStatus;
import net.mozq.picto.view.Messages;

public class ProcessCore {
	
	private static final String FILE_DIGEST_ALGORITHM = "MD5";
	
	private static final String EXIF_DATE_PATTERN = "yyyy:MM:dd HH:mm:ss";
	private static final String EXIF_SUBSEC_PATTERN = "00";
	
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
	
	public ProcessCore() {
		// NOP
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
				processCondition.getDept(),
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

						ImageMetadata imageMetadata = getImageMetadata(file);
						
						Date baseDate;
						if (processCondition.isChangeFileCreationDate()
								|| processCondition.isChangeFileModifiedDate()
								|| processCondition.isChangeFileAccessDate()
								|| processCondition.isChangeExifDate()
								) {
							baseDate = getBaseDate(processCondition, file, attrs, imageMetadata);
						} else {
							baseDate = null;
						}
						
						String destSubPathname = processCondition.getDestSubPathFormat().format(varName -> {
							try {
								switch (varName) {
								case "Now": return new Date();
								case "ParentSubPath": return rootRelativeSubPath.toString();
								case "FileName": return file.getFileName().toString();
								case "BaseName": return FileUtilz.getBaseName(file.getFileName().toString());
								case "Extension": return FileUtilz.getExt(file.getFileName().toString());
								case "Size": return Long.valueOf(Files.size(file));
								case "CreationDate": return (processCondition.isChangeFileCreationDate()) ? baseDate : new Date(attrs.creationTime().toMillis());
								case "ModifiedDate": return (processCondition.isChangeFileModifiedDate()) ? baseDate : new Date(attrs.lastModifiedTime().toMillis());
								case "AccessDate": return (processCondition.isChangeFileAccessDate()) ? baseDate : new Date(attrs.lastAccessTime().toMillis());
								case "PhotoTakenDate": return (processCondition.isChangeExifDate()) ? baseDate : getPhotoTakenDate(file, imageMetadata);
								case "Width": return getEXIFIntValue(imageMetadata, ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);
								case "Height": return getEXIFIntValue(imageMetadata, ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH);
								case "FNumber": return getEXIFDoubleValue(imageMetadata, ExifTagConstants.EXIF_TAG_FNUMBER);
								case "Aperture": return getEXIFDoubleValue(imageMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
								case "MaxAperture": return getEXIFDoubleValue(imageMetadata, ExifTagConstants.EXIF_TAG_MAX_APERTURE_VALUE);
								case "ISO": return getEXIFIntValue(imageMetadata, ExifTagConstants.EXIF_TAG_ISO);
								case "FocalLength": return getEXIFDoubleValue(imageMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH); // 焦点距離
								case "FocalLength35mm": return getEXIFDoubleValue(imageMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT);
								case "ShutterSpeed": return getEXIFDoubleValue(imageMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
								case "Exposure": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE); // 露出
								case "ExposureTime": return getEXIFDoubleValue(imageMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_TIME); // 露出時間（秒）
								case "ExposureMode": return getEXIFIntValue(imageMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_MODE);
								case "ExposureProgram": return getEXIFIntValue(imageMetadata, ExifTagConstants.EXIF_TAG_EXPOSURE_PROGRAM);
								case "Brightness": return getEXIFDoubleValue(imageMetadata, ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE);
								case "WhiteBalance": return getEXIFIntValue(imageMetadata, ExifTagConstants.EXIF_TAG_WHITE_BALANCE_1);
								case "LightSource": return getEXIFIntValue(imageMetadata, ExifTagConstants.EXIF_TAG_LIGHT_SOURCE);
								case "Lens": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_LENS);
								case "LensMake": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_LENS_MAKE);
								case "LensModel": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_LENS_MODEL);
								case "LensSerialNumber": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_LENS_SERIAL_NUMBER);
								case "Make": return getEXIFStringValue(imageMetadata, TiffTagConstants.TIFF_TAG_MAKE);
								case "Model": return getEXIFStringValue(imageMetadata, TiffTagConstants.TIFF_TAG_MODEL);
								case "SerialNumber": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_SERIAL_NUMBER);
								case "Software": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_SOFTWARE);
								case "ProcessingSoftware": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_PROCESSING_SOFTWARE);
								case "OwnerName": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_OWNER_NAME);
								case "CameraOwnerName": return getEXIFStringValue(imageMetadata, ExifTagConstants.EXIF_TAG_CAMERA_OWNER_NAME);
								case "GPSLat": return getEXIFGpsLat(imageMetadata);
								case "GPSLatDeg": return getEXIFDoubleValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE, 0);
								case "GPSLatMin": return getEXIFDoubleValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE, 1);
								case "GPSLatSec": return getEXIFDoubleValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE, 2);
								case "GPSLatRef": return getEXIFStringValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
								case "GPSLon": return getEXIFGpsLon(imageMetadata);
								case "GPSLonDeg": return getEXIFDoubleValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE, 0);
								case "GPSLonMin": return getEXIFDoubleValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE, 1);
								case "GPSLonSec": return getEXIFDoubleValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE, 2);
								case "GPSLonRef": return getEXIFStringValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
								case "GPSAlt": return getEXIFDoubleValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
								case "GPSAltRef": return getEXIFIntValue(imageMetadata, GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
								default: throw new PictoInvalidDestinationPathException(
											Messages.getString("message.warn.invalid.destSubPath.varName", varName)
											);
								}
							} catch (PictoException e) {
								throw e;
							} catch (Exception e) {
								throw new PictoInvalidDestinationPathException(
										Messages.getString("message.warn.invalid.destSubPath.pattern"),
										e
										);
							}
						});
						
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
			Function<Integer, ProcessData> processDataGetter,
			IntConsumer processDataUpdater,
			Function<ProcessData, ProcessDataStatus> overwriteConfirm,
			BooleanSupplier processStopper
			) throws IOException {

		int index = 0;
		while (!processStopper.getAsBoolean()) {
			
			ProcessData processData = processDataGetter.apply(index);
			if (processData == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// NOP
				}
				continue;
			}

			processData.setStatus(ProcessDataStatus.Processing);
			processDataUpdater.accept(index);
			
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
			
			processDataUpdater.accept(index);
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
		case Overwrite: // FALLTHRU
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
		
		Path destParentPath = processData.getDestPath().getParent();
		if (destParentPath != null) {
			Files.createDirectories(destParentPath);
		}
		
		if (processCondition.isCheckDigest()
				|| (processCondition.isChangeExifDate() && processData.getBaseDate() != null)
				|| processCondition.isRemveExifTagsGps()
				|| processCondition.isRemveExifTagsAll()
				) {
			Path destTempPath = null;
			try {
				destTempPath = Files.createTempFile(processData.getDestPath().getParent(), processData.getDestPath().getFileName().toString(), null);
				
				if (processCondition.isCheckDigest()) {
					String algorithm = FILE_DIGEST_ALGORITHM;
					
					MessageDigest srcMD = newMessageDigest(algorithm);
					try (InputStream is = new DigestInputStream(new BufferedInputStream(Files.newInputStream(processData.getSrcPath())), srcMD)) {
						Files.copy(is, destTempPath, OPTIONS_COPY_REPLACE);
					}
					byte[] srcDigest = srcMD.digest();
					
					MessageDigest destMD = newMessageDigest(algorithm);
					try (InputStream is = new DigestInputStream(new BufferedInputStream(Files.newInputStream(destTempPath)), destMD)) {
						byte[] b = new byte[1024];
						while(is.read(b) != -1) { }
					}
					byte[] destDigest = destMD.digest();
					
					if (isSame(srcDigest, destDigest)) {
						throw new PictoFileDigestMismatchException(Messages.getString("message.error.digest.mismatch"));
					}
				} else if (processCondition.isRemveExifTagsAll()) {
					ExifRewriter exifRewriter = new ExifRewriter();
					try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(destTempPath))) {
						exifRewriter.removeExifMetadata(processData.getSrcPath().toFile(), os);
					} catch (ImageReadException | ImageWriteException e) {
						throw new PictoFileChangeException(Messages.getString("message.error.edit.file"), e);
					}
				} else if (processCondition.isChangeExifDate() || processCondition.isRemveExifTagsGps()) {
					ImageMetadata imageMetadata = getImageMetadata(processData.getSrcPath());
					TiffOutputSet outputSet = getOutputSet(imageMetadata);
					if (outputSet == null) {
						Files.copy(processData.getSrcPath(), destTempPath, OPTIONS_COPY_REPLACE);
					} else {
						if (processCondition.isChangeExifDate()) {
							SimpleDateFormat exifDateFormat = new SimpleDateFormat(EXIF_DATE_PATTERN);
							exifDateFormat.setTimeZone(processCondition.getTimeZone());
							String exifBaseDate = exifDateFormat.format(processData.getBaseDate());
							
							DecimalFormat exifSubsecFormat = new DecimalFormat(EXIF_SUBSEC_PATTERN);
							String exifBaseSubsec = exifSubsecFormat.format((int)(processData.getBaseDate().getTime() / 10) % 100);
							
							try {
								TiffOutputDirectory rootDirectory = outputSet.getRootDirectory();
								TiffOutputDirectory exifDirectory = outputSet.getExifDirectory();
								if (rootDirectory != null) {
									rootDirectory.removeField(TiffTagConstants.TIFF_TAG_DATE_TIME);
									rootDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, exifBaseDate);
								}
								if (exifDirectory != null) {
									exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME);
									exifDirectory.add(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME, exifBaseSubsec);
									
									exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
									exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, exifBaseDate);
									exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_ORIGINAL);
									exifDirectory.add(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_ORIGINAL, exifBaseSubsec);
									
									exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
									exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, exifBaseDate);
									exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_DIGITIZED);
									exifDirectory.add(ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_DIGITIZED, exifBaseSubsec);
								}
							} catch (ImageWriteException e) {
								throw new PictoFileChangeException(Messages.getString("message.error.edit.file"), e);
							}
						}
						
						if (processCondition.isRemveExifTagsGps()) {
							outputSet.removeField(ExifTagConstants.EXIF_TAG_GPSINFO);
						}
						
						ExifRewriter exifRewriter = new ExifRewriter();
						try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(destTempPath))) {
							exifRewriter.updateExifMetadataLossless(processData.getSrcPath().toFile(), os, outputSet);
						} catch (ImageReadException | ImageWriteException e) {
							throw new PictoFileChangeException(Messages.getString("message.error.edit.file"), e);
						}
					}
				}
				
				Path destPath;
				if (processCondition.getOperationType() == OperationType.Overwrite) {
					destPath = processData.getSrcPath();
				} else {
					destPath = processData.getDestPath();
				}
				try {
					Files.move(destTempPath, destPath, OPTIONS_MOVE);
					if (processCondition.getOperationType() == OperationType.Move) {
						Files.deleteIfExists(processData.getSrcPath());
					}
					status = ProcessDataStatus.Success;
				} catch (FileAlreadyExistsException e) {
					status = confirmOverwrite(processCondition, processData, overwriteConfirm);
					if (status == ProcessDataStatus.Processing) {
						// Overwrite
						Files.move(destTempPath, destPath, OPTIONS_MOVE_REPLACE);
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
					Files.copy(processData.getSrcPath(), processData.getDestPath(), OPTIONS_COPY);
					status = ProcessDataStatus.Success;
				} catch (FileAlreadyExistsException e) {
					status = confirmOverwrite(processCondition, processData, overwriteConfirm);
					if (status == ProcessDataStatus.Processing) {
						Files.copy(processData.getSrcPath(), processData.getDestPath(), OPTIONS_COPY_REPLACE);
						status = ProcessDataStatus.Success;
					}
				}
				break;
			case Move:
				try {
					Files.move(processData.getSrcPath(), processData.getDestPath(), OPTIONS_MOVE);
					status = ProcessDataStatus.Success;
				} catch (FileAlreadyExistsException e) {
					status = confirmOverwrite(processCondition, processData, overwriteConfirm);
					if (status == ProcessDataStatus.Processing) {
						Files.move(processData.getSrcPath(), processData.getDestPath(), OPTIONS_MOVE_REPLACE);
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
			BasicFileAttributeView attributeView = Files.getFileAttributeView(processData.getDestPath(), BasicFileAttributeView.class);
			attributeView.setTimes(modifiedFileTime, accessFileTime, creationFileTime);
		}
		
		return status;
	}
	
	private static Date getBaseDate(
			ProcessCondition processCondition,
			Path file, BasicFileAttributes attrs,
			ImageMetadata imageMetadata
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
			baseDate = getExifDate(imageMetadata);
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
	
	private static Date getPhotoTakenDate(Path imagePath, ImageMetadata imageImageMetadata) {
		File imageFile = imagePath.toFile();
		
		Date photoTakenDate;
		try {
			photoTakenDate = getExifDate(imageImageMetadata);
		} catch (IOException e) {
			photoTakenDate = null;
		}
		
		if (photoTakenDate == null) {
			photoTakenDate = new Date(imageFile.lastModified());
		}
		
		return photoTakenDate;
	}
	
	private static ImageMetadata getImageMetadata(Path imagePath) throws IOException {
		File imageFile = imagePath.toFile();
		
		ImageMetadata imageMetadata;
		try {
			imageMetadata = Imaging.getMetadata(imageFile);
		} catch (ImageReadException e) {
			imageMetadata = null;
		}
		
		return imageMetadata;
	}
	
	private static TiffOutputSet getOutputSet(ImageMetadata imageMetadata) {
		if (imageMetadata == null) {
			return null;
		}

		TiffOutputSet outputSet = null;
		if (imageMetadata instanceof JpegImageMetadata) {
			try {
				TiffImageMetadata exifMetadata = ((JpegImageMetadata)imageMetadata).getExif();
				if (exifMetadata != null) {
					outputSet = exifMetadata.getOutputSet();
				}
			} catch (ImageWriteException e) {
				// NOP
			}
		} else if (imageMetadata instanceof TiffImageMetadata) {
			try {
				outputSet = ((TiffImageMetadata)imageMetadata).getOutputSet();
			} catch (ImageWriteException e) {
				// NOP
			}
		}
		
		return outputSet;
	}
	
	private static Date getExifDate(ImageMetadata imageMetadata) throws IOException {
		Date photoTakenDate = getEXIFDateValue(imageMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_ORIGINAL);
		if (photoTakenDate == null) {
			photoTakenDate = getEXIFDateValue(imageMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_DIGITIZED);
			if (photoTakenDate == null) {
				photoTakenDate = getEXIFDateValue(imageMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME);
			}
		}
		
		return photoTakenDate;
	}
	
	private static TiffField getTiffField(ImageMetadata imageMetadata, TagInfo tagInfo) {
		if (imageMetadata == null) {
			return null;
		}
		
		TiffField field;
		if (imageMetadata instanceof JpegImageMetadata) {
			field = ((JpegImageMetadata)imageMetadata).findEXIFValueWithExactMatch(tagInfo);
		} else if (imageMetadata instanceof TiffImageMetadata) {
			try {
				field = ((TiffImageMetadata)imageMetadata).findField(tagInfo, true);
			} catch (ImageReadException e) {
				field = null;
			}
		} else {
			field = null;
		}
		
		return field;
	}
	
	private static String getEXIFStringValue(ImageMetadata imageMetadata, TagInfo tagInfo) {
		TiffField field = getTiffField(imageMetadata, tagInfo);
		if (field == null) {
			return null;
		}
		
		String exifStr;
		try {
			exifStr = field.getStringValue();
		} catch (ImageReadException e) {
			return null;
		}
		if (exifStr != null) {
			int nullIdx = exifStr.indexOf('\u0000');
			if (nullIdx != -1) {
				exifStr = exifStr.substring(0, nullIdx);
			}
		}
		
		return exifStr;
	}
	
	private static Integer getEXIFIntValue(ImageMetadata imageMetadata, TagInfo tagInfo) {
		TiffField field = getTiffField(imageMetadata, tagInfo);
		if (field == null) {
			return null;
		}
		
		Integer value;
		try {
			value = Integer.valueOf(field.getIntValue());
		} catch (ImageReadException e) {
			value = null;
		}
		
		return value;
	}
	
	private static Double getEXIFDoubleValue(ImageMetadata imageMetadata, TagInfo tagInfo) {
		TiffField field = getTiffField(imageMetadata, tagInfo);
		if (field == null) {
			return null;
		}
		
		Double value;
		try {
			value = Double.valueOf(field.getDoubleValue());
		} catch (ImageReadException e) {
			value = null;
		}
		
		return value;
	}
	
	private static Double getEXIFDoubleValue(ImageMetadata imageMetadata, TagInfo tagInfo, int index) {
		TiffField field = getTiffField(imageMetadata, tagInfo);
		if (field == null) {
			return null;
		}
		
		Double value;
		try {
			double[] v = field.getDoubleArrayValue();
			if (v == null) {
				value = null;
			} else {
				value = Double.valueOf(v[index]);
			}
		} catch (ImageReadException e) {
			value = null;
		}
		
		return value;
	}
	
	private static Date getEXIFDateValue(ImageMetadata imageMetadata, TagInfo tagInfo, TagInfo subTagInfo) {
		if (imageMetadata == null) {
			return null;
		}
		
		String exifDateStr = getEXIFStringValue(imageMetadata, tagInfo);
		if (exifDateStr == null) {
			return null;
		}
		
		Date date;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(EXIF_DATE_PATTERN);
			date = dateFormat.parse(exifDateStr);
		} catch (ParseException e) {
			date = null;
		}
		if (date != null && subTagInfo != null) {
			String subSec = getEXIFStringValue(imageMetadata, subTagInfo);
			if (subSec != null && !subSec.isEmpty()) {
				try {
					date = new Date(date.getTime() + (Integer.parseInt(subSec) * 10));
				} catch (NumberFormatException e) {
					// NOP
				}
			}
		}
		
		return date;
	}
	
	private static GPSInfo getEXIFGpsInfo(ImageMetadata imageMetadata) {
		if (imageMetadata == null) {
			return null;
		}
		
		TiffImageMetadata tiffImageMetadata = null;
		if (imageMetadata instanceof JpegImageMetadata) {
			JpegImageMetadata jpegMetadata = (JpegImageMetadata)imageMetadata;
			tiffImageMetadata = jpegMetadata.getExif();
		} else if (imageMetadata instanceof TiffImageMetadata) {
			tiffImageMetadata = (TiffImageMetadata)imageMetadata;
		}
		
		if (tiffImageMetadata == null) {
			return null;
		}
		
		GPSInfo gpsInfo;
		try {
			gpsInfo = tiffImageMetadata.getGPS();
		} catch (ImageReadException e) {
			return null;
		}
		
		return gpsInfo;
	}
	
	private static Double getEXIFGpsLat(ImageMetadata imageMetadata) {
		GPSInfo gpsInfo = getEXIFGpsInfo(imageMetadata);
		if (gpsInfo == null) {
			return null;
		}
		
		try {
			return Double.valueOf(gpsInfo.getLatitudeAsDegreesNorth());
		} catch (ImageReadException e) {
			return null;
		}
	}
	
	private static Double getEXIFGpsLon(ImageMetadata imageMetadata) {
		GPSInfo gpsInfo = getEXIFGpsInfo(imageMetadata);
		if (gpsInfo == null) {
			return null;
		}
		
		try {
			return Double.valueOf(gpsInfo.getLongitudeAsDegreesEast());
		} catch (ImageReadException e) {
			return null;
		}
	}
	
	private static MessageDigest newMessageDigest(String algorithm) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return md;
	}
	
	private static boolean isSame(byte[] b1, byte[] b2) {
		if (b1 == null || b2 == null) {
			return (b1 == null && b2 == null);
		}
		
		if (b1.length != b2.length) {
			return false;
		}
		
		for (int i = 0; i < b1.length; i++) {
			if (b1[i] != b2[i]) {
				return false;
			}
		}
		
		return true;
	}
}
