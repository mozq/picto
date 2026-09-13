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
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.function.Supplier;

import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;

import net.mozq.picto.core.exception.PictoInvalidDestinationPathException;
import net.mozq.picto.util.FileNameSupport;
import net.mozq.picto.view.Messages;

final class TemplateVariables {

	private final ProcessCondition processCondition;
	private final Path file;
	private final BasicFileAttributes attrs;
	private final Path rootRelativeSubPath;
	private final Supplier<ImageMetadata> imageMetadataSupplier;
	private final Date baseDate;

	TemplateVariables(
			ProcessCondition processCondition,
			Path file,
			BasicFileAttributes attrs,
			Path rootRelativeSubPath,
			Supplier<ImageMetadata> imageMetadataSupplier,
			Date baseDate) {
		this.processCondition = processCondition;
		this.file = file;
		this.attrs = attrs;
		this.rootRelativeSubPath = rootRelativeSubPath;
		this.imageMetadataSupplier = imageMetadataSupplier;
		this.baseDate = baseDate;
	}

	Object resolve(String varName) throws IOException {
		switch (varName) {
		case "Now": return new Date();
		case "SubFilePath": return processCondition.getSrcFolder().relativize(file).toString();
		case "SubFolderPath": return rootRelativeSubPath.toString();
		case "FileName": return file.getFileName().toString();
		case "BaseName": return FileNameSupport.baseName(file.getFileName().toString());
		case "Extension": return FileNameSupport.extension(file.getFileName().toString());
		case "Size": return Long.valueOf(attrs.size());
		case "CreationDate": return processCondition.isChangeFileCreationDate() ? baseDate : new Date(attrs.creationTime().toMillis());
		case "ModifiedDate": return processCondition.isChangeFileModifiedDate() ? baseDate : new Date(attrs.lastModifiedTime().toMillis());
		case "AccessDate": return processCondition.isChangeFileAccessDate() ? baseDate : new Date(attrs.lastAccessTime().toMillis());
		case "TakenDate": return processCondition.isChangeFileExifDate() ? baseDate : ExifMetadataSupport.photoTakenDate(file, imageMetadata());
		case "Width": return ExifMetadataSupport.intValue(imageMetadata(), ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);
		case "Height": return ExifMetadataSupport.intValue(imageMetadata(), ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH);
		case "FNumber": return ExifMetadataSupport.doubleValue(imageMetadata(), ExifTagConstants.EXIF_TAG_FNUMBER);
		case "Aperture": return ExifMetadataSupport.doubleValue(imageMetadata(), ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
		case "MaxAperture": return ExifMetadataSupport.doubleValue(imageMetadata(), ExifTagConstants.EXIF_TAG_MAX_APERTURE_VALUE);
		case "ISO": return ExifMetadataSupport.intValue(imageMetadata(), ExifTagConstants.EXIF_TAG_ISO);
		case "FocalLength": return ExifMetadataSupport.doubleValue(imageMetadata(), ExifTagConstants.EXIF_TAG_FOCAL_LENGTH);
		case "FocalLength35mm": return ExifMetadataSupport.doubleValue(imageMetadata(), ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT);
		case "ShutterSpeed": return ExifMetadataSupport.doubleValue(imageMetadata(), ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
		case "Exposure": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_EXPOSURE);
		case "ExposureTime": return ExifMetadataSupport.doubleValue(imageMetadata(), ExifTagConstants.EXIF_TAG_EXPOSURE_TIME);
		case "ExposureMode": return ExifMetadataSupport.intValue(imageMetadata(), ExifTagConstants.EXIF_TAG_EXPOSURE_MODE);
		case "ExposureProgram": return ExifMetadataSupport.intValue(imageMetadata(), ExifTagConstants.EXIF_TAG_EXPOSURE_PROGRAM);
		case "Brightness": return ExifMetadataSupport.doubleValue(imageMetadata(), ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE);
		case "WhiteBalance": return ExifMetadataSupport.intValue(imageMetadata(), ExifTagConstants.EXIF_TAG_WHITE_BALANCE_1);
		case "LightSource": return ExifMetadataSupport.intValue(imageMetadata(), ExifTagConstants.EXIF_TAG_LIGHT_SOURCE);
		case "Orientation": return ExifMetadataSupport.intValue(imageMetadata(), TiffTagConstants.TIFF_TAG_ORIENTATION);
		case "Lens": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_LENS);
		case "LensMake": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_LENS_MAKE);
		case "LensModel": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_LENS_MODEL);
		case "LensSerialNumber": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_LENS_SERIAL_NUMBER);
		case "Make": return ExifMetadataSupport.stringValue(imageMetadata(), TiffTagConstants.TIFF_TAG_MAKE);
		case "Model": return ExifMetadataSupport.stringValue(imageMetadata(), TiffTagConstants.TIFF_TAG_MODEL);
		case "SerialNumber": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_SERIAL_NUMBER);
		case "Software": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_SOFTWARE);
		case "ProcessingSoftware": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_PROCESSING_SOFTWARE);
		case "OwnerName": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_OWNER_NAME);
		case "CameraOwnerName": return ExifMetadataSupport.stringValue(imageMetadata(), ExifTagConstants.EXIF_TAG_CAMERA_OWNER_NAME);
		case "GPSLat": return ExifMetadataSupport.gpsLatitude(imageMetadata());
		case "GPSLatDeg": return ExifMetadataSupport.doubleValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_LATITUDE, 0);
		case "GPSLatMin": return ExifMetadataSupport.doubleValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_LATITUDE, 1);
		case "GPSLatSec": return ExifMetadataSupport.doubleValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_LATITUDE, 2);
		case "GPSLatRef": return ExifMetadataSupport.stringValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
		case "GPSLon": return ExifMetadataSupport.gpsLongitude(imageMetadata());
		case "GPSLonDeg": return ExifMetadataSupport.doubleValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_LONGITUDE, 0);
		case "GPSLonMin": return ExifMetadataSupport.doubleValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_LONGITUDE, 1);
		case "GPSLonSec": return ExifMetadataSupport.doubleValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_LONGITUDE, 2);
		case "GPSLonRef": return ExifMetadataSupport.stringValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
		case "GPSAlt": return ExifMetadataSupport.doubleValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
		case "GPSAltRef": return ExifMetadataSupport.intValue(imageMetadata(), GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
		default:
			throw new PictoInvalidDestinationPathException(
					Messages.getString("message.warn.invalid.destSubFilePath.varName", varName)
					);
		}
	}

	private ImageMetadata imageMetadata() {
		return imageMetadataSupplier.get();
	}
}
