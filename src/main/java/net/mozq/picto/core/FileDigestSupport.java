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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import net.mozq.picto.core.exception.PictoFileDigestMismatchException;
import net.mozq.picto.view.Messages;

final class FileDigestSupport {

	private static final String FILE_DIGEST_ALGORITHM = "MD5";

	private FileDigestSupport() {
	}

	static void copyAndVerify(Path sourcePath, Path destinationPath) throws IOException {
		MessageDigest sourceDigest = newMessageDigest();
		try (InputStream is = new DigestInputStream(new BufferedInputStream(Files.newInputStream(sourcePath)), sourceDigest)) {
			Files.copy(is, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		}

		MessageDigest destinationDigest = newMessageDigest();
		try (InputStream is = new DigestInputStream(new BufferedInputStream(Files.newInputStream(destinationPath)), destinationDigest)) {
			byte[] buffer = new byte[1024];
			while (is.read(buffer) != -1) {
				// Read through the stream to update the digest.
			}
		}

		if (!Arrays.equals(sourceDigest.digest(), destinationDigest.digest())) {
			throw new PictoFileDigestMismatchException(Messages.getString("message.error.digest.mismatch"));
		}
	}

	private static MessageDigest newMessageDigest() {
		try {
			return MessageDigest.getInstance(FILE_DIGEST_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
