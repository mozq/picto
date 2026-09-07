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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata.GpsInfo;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import net.mozq.picto.core.exception.PictoFileChangeException;
import net.mozq.picto.view.Messages;

final class ExifMetadataSupport {

	private static final String EXIF_DATE_PATTERN = "yyyy:MM:dd HH:mm:ss";
	private static final String EXIF_SUBSEC_PATTERN = "00";
	private static final DateTimeFormatter EXIF_DATE_FORMATTER = DateTimeFormatter.ofPattern(EXIF_DATE_PATTERN);

	private ExifMetadataSupport() {
	}

	static ImageMetadata loadMetadata(Path imagePath) {
		try {
			return Imaging.getMetadata(imagePath.toFile());
		} catch (IOException | IllegalArgumentException _) {
			return null;
		}
	}

	static Date photoTakenDate(Path imagePath, ImageMetadata imageMetadata) {
		Date photoTakenDate = exifDate(imageMetadata);
		return photoTakenDate != null ? photoTakenDate : new Date(imagePath.toFile().lastModified());
	}

	static void removeAll(Path sourcePath, Path destinationPath) throws IOException {
		ExifRewriter exifRewriter = new ExifRewriter();
		try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(destinationPath))) {
			exifRewriter.removeExifMetadata(sourcePath.toFile(), os);
		} catch (ImagingException e) {
			throw new PictoFileChangeException(Messages.getString("message.error.edit.file"), e);
		}
	}

	static void updateLossless(Path sourcePath, Path destinationPath, Date exifDate, TimeZone timeZone, boolean removeGps) throws IOException {
		ImageMetadata imageMetadata = loadMetadata(sourcePath);
		TiffOutputSet outputSet = outputSet(imageMetadata);
		if (outputSet == null) {
			Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			return;
		}

		if (exifDate != null) {
			updateDate(outputSet, exifDate, timeZone);
		}
		if (removeGps) {
			removeGps(outputSet);
		}

		ExifRewriter exifRewriter = new ExifRewriter();
		try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(destinationPath))) {
			exifRewriter.updateExifMetadataLossless(sourcePath.toFile(), os, outputSet);
		} catch (ImagingException e) {
			throw new PictoFileChangeException(Messages.getString("message.error.edit.file"), e);
		}
	}

	static Date exifDate(ImageMetadata imageMetadata) {
		Date photoTakenDate = exifDateValue(imageMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_ORIGINAL);
		if (photoTakenDate == null) {
			photoTakenDate = exifDateValue(imageMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_DIGITIZED);
			if (photoTakenDate == null) {
				photoTakenDate = exifDateValue(imageMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME);
			}
		}
		return photoTakenDate;
	}

	static String stringValue(ImageMetadata imageMetadata, TagInfo tagInfo) {
		TiffField field = tiffField(imageMetadata, tagInfo);
		if (field == null) {
			return null;
		}
		try {
			String value = field.getStringValue();
			if (value != null) {
				int nullIdx = value.indexOf('\u0000');
				if (nullIdx != -1) {
					value = value.substring(0, nullIdx);
				}
			}
			return value;
		} catch (ImagingException _) {
			return null;
		}
	}

	static Integer intValue(ImageMetadata imageMetadata, TagInfo tagInfo) {
		TiffField field = tiffField(imageMetadata, tagInfo);
		if (field == null) {
			return null;
		}
		try {
			return Integer.valueOf(field.getIntValue());
		} catch (ImagingException _) {
			return null;
		}
	}

	static Double doubleValue(ImageMetadata imageMetadata, TagInfo tagInfo) {
		TiffField field = tiffField(imageMetadata, tagInfo);
		if (field == null) {
			return null;
		}
		try {
			return Double.valueOf(field.getDoubleValue());
		} catch (ImagingException _) {
			return null;
		}
	}

	static Double doubleValue(ImageMetadata imageMetadata, TagInfo tagInfo, int index) {
		TiffField field = tiffField(imageMetadata, tagInfo);
		if (field == null) {
			return null;
		}
		try {
			double[] values = field.getDoubleArrayValue();
			return values == null ? null : Double.valueOf(values[index]);
		} catch (ArrayIndexOutOfBoundsException | ImagingException _) {
			return null;
		}
	}

	static Double gpsLatitude(ImageMetadata imageMetadata) {
		GpsInfo gpsInfo = gpsInfo(imageMetadata);
		if (gpsInfo == null) {
			return null;
		}
		try {
			return Double.valueOf(gpsInfo.getLatitudeAsDegreesNorth());
		} catch (ImagingException _) {
			return null;
		}
	}

	static Double gpsLongitude(ImageMetadata imageMetadata) {
		GpsInfo gpsInfo = gpsInfo(imageMetadata);
		if (gpsInfo == null) {
			return null;
		}
		try {
			return Double.valueOf(gpsInfo.getLongitudeAsDegreesEast());
		} catch (ImagingException _) {
			return null;
		}
	}

	private static void updateDate(TiffOutputSet outputSet, Date exifDate, TimeZone timeZone) {
		String exifBaseDate = EXIF_DATE_FORMATTER.withZone(timeZone.toZoneId()).format(exifDate.toInstant());
		String exifBaseSubsec = new DecimalFormat(EXIF_SUBSEC_PATTERN).format((int)(exifDate.getTime() / 10) % 100);

		try {
			TiffOutputDirectory rootDirectory = outputSet.getRootDirectory();
			TiffOutputDirectory exifDirectory = outputSet.getExifDirectory();
			if (rootDirectory != null) {
				rootDirectory.removeField(TiffTagConstants.TIFF_TAG_DATE_TIME);
				rootDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, exifBaseDate);
			}
			if (exifDirectory != null) {
				replaceDateField(exifDirectory, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME, exifBaseSubsec);
				replaceDateField(exifDirectory, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, exifBaseDate);
				replaceDateField(exifDirectory, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_ORIGINAL, exifBaseSubsec);
				replaceDateField(exifDirectory, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, exifBaseDate);
				replaceDateField(exifDirectory, ExifTagConstants.EXIF_TAG_SUB_SEC_TIME_DIGITIZED, exifBaseSubsec);
			}
		} catch (ImagingException e) {
			throw new PictoFileChangeException(Messages.getString("message.error.edit.file"), e);
		}
	}

	private static void replaceDateField(TiffOutputDirectory directory, TagInfoAscii tagInfo, String value) throws ImagingException {
		directory.removeField(tagInfo);
		directory.add(tagInfo, value);
	}

	private static void removeGps(TiffOutputSet outputSet) {
		outputSet.removeField(ExifTagConstants.EXIF_TAG_GPSINFO);
		TiffOutputDirectory gpsDirectory = outputSet.getGpsDirectory();
		if (gpsDirectory != null) {
			GpsTagConstants.ALL_GPS_TAGS.forEach(gpsDirectory::removeField);
		}
	}

	private static TiffOutputSet outputSet(ImageMetadata imageMetadata) {
		if (imageMetadata == null) {
			return null;
		}
		try {
			if (imageMetadata instanceof JpegImageMetadata) {
				TiffImageMetadata exifMetadata = ((JpegImageMetadata)imageMetadata).getExif();
				return exifMetadata != null ? exifMetadata.getOutputSet() : null;
			}
			if (imageMetadata instanceof TiffImageMetadata) {
				return ((TiffImageMetadata)imageMetadata).getOutputSet();
			}
			return null;
		} catch (ImagingException _) {
			return null;
		}
	}

	private static TiffField tiffField(ImageMetadata imageMetadata, TagInfo tagInfo) {
		if (imageMetadata == null) {
			return null;
		}
		if (imageMetadata instanceof JpegImageMetadata) {
			return ((JpegImageMetadata)imageMetadata).findExifValueWithExactMatch(tagInfo);
		}
		if (imageMetadata instanceof TiffImageMetadata) {
			try {
				return ((TiffImageMetadata)imageMetadata).findField(tagInfo, true);
			} catch (ImagingException _) {
				return null;
			}
		}
		return null;
	}

	private static Date exifDateValue(ImageMetadata imageMetadata, TagInfo tagInfo, TagInfo subTagInfo) {
		if (imageMetadata == null) {
			return null;
		}
		String exifDateStr = stringValue(imageMetadata, tagInfo);
		if (exifDateStr == null) {
			return null;
		}

		Date date;
		try {
			LocalDateTime ldt = LocalDateTime.parse(exifDateStr, EXIF_DATE_FORMATTER);
			date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		} catch (DateTimeParseException _) {
			return null;
		}
		if (subTagInfo != null) {
			String subSec = stringValue(imageMetadata, subTagInfo);
			int millis = parseSubSecToMillis(subSec);
			if (millis > 0) {
				date = new Date(date.getTime() + millis);
			}
		}
		return date;
	}

	static int parseSubSecToMillis(String subSec) {
		if (subSec == null || subSec.isEmpty()) {
			return 0;
		}
		String digits = subSec.trim();
		if (!digits.matches("\\d+")) {
			return 0;
		}
		if (digits.length() >= 3) {
			return Integer.parseInt(digits.substring(0, 3));
		} else if (digits.length() == 2) {
			return Integer.parseInt(digits) * 10;
		} else if (digits.length() == 1) {
			return Integer.parseInt(digits) * 100;
		}
		return 0;
	}

	private static GpsInfo gpsInfo(ImageMetadata imageMetadata) {
		if (imageMetadata == null) {
			return null;
		}

		TiffImageMetadata tiffImageMetadata = null;
		if (imageMetadata instanceof JpegImageMetadata) {
			tiffImageMetadata = ((JpegImageMetadata)imageMetadata).getExif();
		} else if (imageMetadata instanceof TiffImageMetadata) {
			tiffImageMetadata = (TiffImageMetadata)imageMetadata;
		}
		if (tiffImageMetadata == null) {
			return null;
		}
		try {
			return tiffImageMetadata.getGpsInfo();
		} catch (ImagingException _) {
			return null;
		}
	}
}
