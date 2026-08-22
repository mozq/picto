/*!
 * Picto
 * Copyright 2016 Mozq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.mifmi.commons4j.text.format.NamedFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.picto.core.exception.PictoInvalidDestinationPathException;
import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.OperationType;
import net.mozq.picto.enums.ProcessDataStatus;

class ProcessCoreTest {

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@TempDir
	Path tempDir;

	@Test
	void copiesFile() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = tempDir.resolve("dest/copied.txt");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertTrue(Files.exists(src));
		assertEquals("source", Files.readString(dest));
	}

	@Test
	void movesFile() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = tempDir.resolve("dest/moved.txt");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Move);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertFalse(Files.exists(src));
		assertEquals("source", Files.readString(dest));
	}

	@Test
	void skipsExistingDestinationWhenConfiguredToSkip() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = Files.writeString(tempDir.resolve("dest.txt"), "existing");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setExistingFileMethod(ExistingFileMethod.Skip);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Skipped, data.getStatus());
		assertEquals("existing", Files.readString(dest));
	}

	@Test
	void overwritesExistingDestinationWhenConfirmed() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = Files.writeString(tempDir.resolve("dest.txt"), "existing");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setExistingFileMethod(ExistingFileMethod.Confirm);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertEquals("source", Files.readString(dest));
	}

	@Test
	void skipsExistingDestinationWhenConfirmCallbackSkips() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = Files.writeString(tempDir.resolve("dest.txt"), "existing");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setExistingFileMethod(ExistingFileMethod.Confirm);

		runSingle(condition, data, ignored -> ProcessDataStatus.Skipped);

		assertEquals(ProcessDataStatus.Skipped, data.getStatus());
		assertEquals("existing", Files.readString(dest));
	}

	@Test
	void terminatesWhenExistingDestinationIsConfiguredToTerminate() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = Files.writeString(tempDir.resolve("dest.txt"), "existing");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setExistingFileMethod(ExistingFileMethod.Terminate);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Terminated, data.getStatus());
		assertEquals("existing", Files.readString(dest));
	}

	@Test
	void movesAndOverwritesExistingDestinationWhenConfirmed() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = Files.writeString(tempDir.resolve("dest.txt"), "existing");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Move);
		condition.setExistingFileMethod(ExistingFileMethod.Confirm);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertFalse(Files.exists(src));
		assertEquals("source", Files.readString(dest));
	}

	@Test
	void dryRunDoesNotCopyFile() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = tempDir.resolve("dest.txt");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setDryRun(true);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertFalse(Files.exists(dest));
	}

	@Test
	void changesFileModifiedDate() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = tempDir.resolve("dest.txt");
		Date baseDate = new Date(1_735_689_600_000L);
		ProcessData data = processData(src, dest);
		data.setBaseDate(baseDate);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setChangeFileModifiedDate(true);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertEquals(baseDate.getTime(), Files.getLastModifiedTime(dest).toMillis());
	}

	@Test
	void checkDigestCopiesFileSuccessfully() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = tempDir.resolve("dest.txt");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setCheckDigest(true);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertEquals("source", Files.readString(dest));
	}

	@Test
	void removesAllExifMetadataFromJpeg() throws Exception {
		Path src = jpegWithExif(tempDir.resolve("source.jpg"), "2026:08:20 12:34:56", 35.0, 139.0);
		Path dest = tempDir.resolve("dest.jpg");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setRemveExifTagsAll(true);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertNull(jpegExif(dest));
	}

	@Test
	void removesGpsExifMetadataFromJpeg() throws Exception {
		Path src = jpegWithExif(tempDir.resolve("source.jpg"), "2026:08:20 12:34:56", 35.0, 139.0);
		Path dest = tempDir.resolve("dest.jpg");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setRemveExifTagsGps(true);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		TiffImageMetadata exif = jpegExif(dest);
		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertNotNull(exif);
		assertNull(exif.findField(GpsTagConstants.GPS_TAG_GPS_LATITUDE, true));
		assertNull(exif.findField(GpsTagConstants.GPS_TAG_GPS_LONGITUDE, true));
		assertNotNull(exif.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, true));
	}

	@Test
	void changesExifDateInJpeg() throws Exception {
		Path src = jpegWithExif(tempDir.resolve("source.jpg"), "2026:08:20 12:34:56", 35.0, 139.0);
		Path dest = tempDir.resolve("dest.jpg");
		Date baseDate = parseUtc("2027-01-02 03:04:05");
		ProcessData data = processData(src, dest);
		data.setBaseDate(baseDate);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setChangeExifDate(true);
		condition.setTimeZone(UTC);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		TiffImageMetadata exif = jpegExif(dest);
		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertNotNull(exif);
		assertEquals("2027:01:02 03:04:05", exif.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, true).getStringValue());
		assertEquals("2027:01:02 03:04:05", exif.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, true).getStringValue());
	}

	@Test
	void copiesJpegWithoutExifWhenChangingExifDate() throws Exception {
		Path src = plainJpeg(tempDir.resolve("source.jpg"));
		Path dest = tempDir.resolve("dest.jpg");
		ProcessData data = processData(src, dest);
		data.setBaseDate(parseUtc("2027-01-02 03:04:05"));
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setChangeExifDate(true);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertTrue(Files.exists(dest));
		assertNull(jpegExif(dest));
	}

	@Test
	void copiesNonImageFileWhenRemovingGpsExif() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = tempDir.resolve("dest.txt");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setRemveExifTagsGps(true);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Success, data.getStatus());
		assertEquals("source", Files.readString(dest));
	}

	@Test
	void reportsErrorWhenRemovingAllExifFromNonImageFile() throws IOException {
		Path src = Files.writeString(tempDir.resolve("source.txt"), "source");
		Path dest = tempDir.resolve("dest.txt");
		ProcessData data = processData(src, dest);
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setRemveExifTagsAll(true);

		runSingle(condition, data, ignored -> ProcessDataStatus.Processing);

		assertEquals(ProcessDataStatus.Error, data.getStatus());
		assertFalse(Files.exists(dest));
	}

	@Test
	void findFilesBuildsDestinationPathFromTemplateVariables() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		Path source = Files.writeString(Files.createDirectories(srcRoot.resolve("sub")).resolve("IMG_0001.jpg"), "source");
		ProcessCondition condition = findCondition(srcRoot, destRoot, "${ParentSubPath}/${BaseName}.${Extension}");

		List<ProcessData> files = findFiles(condition);

		assertEquals(1, files.size());
		assertEquals(source, files.get(0).getSrcPath());
		assertEquals(destRoot.resolve("sub/IMG_0001.jpg"), files.get(0).getDestPath());
	}

	@Test
	void findFilesRejectsDestinationPathOutsideRoot() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		Files.writeString(srcRoot.resolve("source.txt"), "source");
		ProcessCondition condition = findCondition(srcRoot, destRoot, "../outside.txt");

		assertThrows(PictoInvalidDestinationPathException.class, () -> findFiles(condition));
	}

	@Test
	void findFilesStopsBeforeVisitingFilesWhenStopperIsSet() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		Files.writeString(srcRoot.resolve("source.txt"), "source");
		ProcessCondition condition = findCondition(srcRoot, destRoot, "${FileName}");
		List<ProcessData> files = new ArrayList<>();

		ProcessCore.findFiles(condition, files::add, () -> true);

		assertTrue(files.isEmpty());
	}

	@Test
	void processFilesStopsBeforeReadingDataWhenStopperIsSet() throws IOException {
		ProcessCondition condition = condition(OperationType.Copy);
		AtomicInteger readCount = new AtomicInteger();

		ProcessCore.processFiles(
				condition,
				index -> {
					readCount.incrementAndGet();
					return null;
				},
				ignored -> {},
				ignored -> ProcessDataStatus.Processing,
				() -> true
				);

		assertEquals(0, readCount.get());
	}

	@Test
	void findFilesUsesDateTimeDigitizedWhenDateTimeOriginalIsMissing() throws Exception {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		jpegWithExifDates(srcRoot.resolve("source.jpg"), null, "2026:08:20 12:34:56", null, false);
		ProcessCondition condition = findCondition(srcRoot, destRoot, "${FileName}");
		condition.setChangeFileModifiedDate(true);
		condition.setBaseDateType(DateType.ExifDate);

		ProcessData data = findFiles(condition).get(0);

		assertEquals(parseDefaultExifDate("2026:08:20 12:34:56"), data.getBaseDate());
	}

	@Test
	void findFilesUsesTiffDateTimeWhenExifDatesAreMissing() throws Exception {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		jpegWithExifDates(srcRoot.resolve("source.jpg"), null, null, "2026:08:20 12:34:56", false);
		ProcessCondition condition = findCondition(srcRoot, destRoot, "${FileName}");
		condition.setChangeFileModifiedDate(true);
		condition.setBaseDateType(DateType.ExifDate);

		ProcessData data = findFiles(condition).get(0);

		assertEquals(parseDefaultExifDate("2026:08:20 12:34:56"), data.getBaseDate());
	}

	@Test
	void findFilesUsesFileModifiedTimeForPhotoTakenDateWhenExifIsMissing() throws Exception {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		Path source = plainJpeg(srcRoot.resolve("source.jpg"));
		Files.setLastModifiedTime(source, FileTime.fromMillis(parseUtc("2026-08-20 12:34:56").getTime()));
		ProcessCondition condition = findCondition(srcRoot, destRoot, "${PhotoTakenDate%uuuu-MM-dd}.txt");
		condition.getDestSubPathFormat().setTimeZone(UTC);

		ProcessData data = findFiles(condition).get(0);

		assertEquals(destRoot.resolve("2026-08-20.txt"), data.getDestPath());
	}

	@Test
	void findFilesAppliesPlusDateModifier() throws Exception {
		ProcessData data = findDataWithDateModifier(DateModType.Plus, 1, 2, 3);

		assertEquals(parseUtc("2026-08-21 14:37:56"), data.getBaseDate());
	}

	@Test
	void findFilesAppliesMinusDateModifier() throws Exception {
		ProcessData data = findDataWithDateModifier(DateModType.Minus, 1, 2, 3);

		assertEquals(parseUtc("2026-08-19 10:31:56"), data.getBaseDate());
	}

	@Test
	void findFilesAppliesOverwriteDateModifier() throws Exception {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		Files.writeString(srcRoot.resolve("source.txt"), "source");
		ProcessCondition condition = findCondition(srcRoot, destRoot, "${FileName}");
		condition.setChangeFileModifiedDate(true);
		condition.setBaseDateType(DateType.CustomDate);
		condition.setCustomBaseDate(parseUtc("2026-08-20 12:34:56"));
		condition.setBaseDateModType(DateModType.Overwrite);
		condition.setBaseDateModYears(2027);
		condition.setBaseDateModDays(2);
		condition.setBaseDateModHours(3);
		condition.setBaseDateModMinutes(4);
		condition.setBaseDateModSeconds(5);

		ProcessData data = findFiles(condition).get(0);

		assertEquals(parseUtc("2027-08-02 03:04:05"), data.getBaseDate());
	}

	private ProcessCondition condition(OperationType operationType) {
		ProcessCondition condition = new ProcessCondition();
		condition.setOperationType(operationType);
		condition.setExistingFileMethod(ExistingFileMethod.Overwrite);
		condition.setTimeZone(UTC);
		return condition;
	}

	private ProcessData processData(Path src, Path dest) throws IOException {
		ProcessData data = new ProcessData();
		data.setSrcPath(src);
		data.setSrcFileAttributes(Files.readAttributes(src, BasicFileAttributes.class));
		data.setDestPath(dest);
		return data;
	}

	private ProcessCondition findCondition(Path srcRoot, Path destRoot, String destSubPathPattern) {
		ProcessCondition condition = condition(OperationType.Copy);
		condition.setSrcRootPath(srcRoot);
		condition.setDestRootPath(destRoot);
		condition.setPathFilter(new PictoPathFilter());
		condition.setDestSubPathFormat(new NamedFormatter(destSubPathPattern));
		condition.setDept(Integer.MAX_VALUE);
		return condition;
	}

	private List<ProcessData> findFiles(ProcessCondition condition) throws IOException {
		List<ProcessData> files = new ArrayList<>();
		ProcessCore.findFiles(condition, files::add, () -> false);
		return files;
	}

	private ProcessData findDataWithDateModifier(DateModType dateModType, int days, int hours, int minutes) throws Exception {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		Files.writeString(srcRoot.resolve("source.txt"), "source");
		ProcessCondition condition = findCondition(srcRoot, destRoot, "${FileName}");
		condition.setChangeFileModifiedDate(true);
		condition.setBaseDateType(DateType.CustomDate);
		condition.setCustomBaseDate(parseUtc("2026-08-20 12:34:56"));
		condition.setBaseDateModType(dateModType);
		condition.setBaseDateModDays(days);
		condition.setBaseDateModHours(hours);
		condition.setBaseDateModMinutes(minutes);
		return findFiles(condition).get(0);
	}

	private void runSingle(
			ProcessCondition condition,
			ProcessData data,
			Function<ProcessData, ProcessDataStatus> overwriteConfirm
			) throws IOException {

		AtomicBoolean stopped = new AtomicBoolean(false);
		ProcessCore.processFiles(
				condition,
				index -> {
					if (index == 0) {
						return data;
					}
					stopped.set(true);
					return null;
				},
				ignored -> {},
				overwriteConfirm,
				stopped::get
				);
	}

	private Path plainJpeg(Path path) throws IOException {
		Path baseJpeg = tempDir.resolve(path.getFileName().toString() + ".base.jpg");
		BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				image.setRGB(x, y, Color.WHITE.getRGB());
			}
		}
		ImageIO.write(image, "jpg", path.toFile());
		Files.deleteIfExists(baseJpeg);
		return path;
	}

	private Path jpegWithExif(Path path, String exifDate, double latitude, double longitude) throws IOException, ImagingException {
		return jpegWithExifDates(path, exifDate, exifDate, null, true, latitude, longitude);
	}

	private Path jpegWithExifDates(Path path, String originalDate, String digitizedDate, String rootDate, boolean withGps) throws IOException, ImagingException {
		return jpegWithExifDates(path, originalDate, digitizedDate, rootDate, withGps, 35.0, 139.0);
	}

	private Path jpegWithExifDates(
			Path path,
			String originalDate,
			String digitizedDate,
			String rootDate,
			boolean withGps,
			double latitude,
			double longitude
			) throws IOException, ImagingException {

		Path baseJpeg = plainJpeg(tempDir.resolve(path.getFileName().toString() + ".base.jpg"));
		TiffOutputSet outputSet = new TiffOutputSet();
		TiffOutputDirectory rootDirectory = outputSet.getOrCreateRootDirectory();
		rootDirectory.add(ExifTagConstants.EXIF_TAG_SOFTWARE, "PictoTest");
		if (rootDate != null) {
			rootDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, rootDate);
		}

		TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
		if (originalDate != null) {
			exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, originalDate);
		}
		if (digitizedDate != null) {
			exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, digitizedDate);
		}
		if (withGps) {
			outputSet.setGpsInDegrees(longitude, latitude);
		}

		try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(path))) {
			new ExifRewriter().updateExifMetadataLossless(baseJpeg.toFile(), os, outputSet);
		}
		Files.deleteIfExists(baseJpeg);
		return path;
	}

	private TiffImageMetadata jpegExif(Path path) throws IOException, ImagingException {
		ImageMetadata metadata = Imaging.getMetadata(path.toFile());
		if (!(metadata instanceof JpegImageMetadata)) {
			return null;
		}
		return ((JpegImageMetadata)metadata).getExif();
	}

	private Date parseUtc(String text) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(UTC);
		return format.parse(text);
	}

	private Date parseDefaultExifDate(String text) throws ParseException {
		return new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(text);
	}
}
