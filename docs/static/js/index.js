(async function () {
	var apiUrl = 'https://api.github.com/repos/mozq/picto/releases/latest';
	var targets = {
		'windows-x64': /-windows-x64\.zip$/,
		'macos-arm64': /-macos-arm64\.dmg$/,
		'linux-x64': /-linux-x64\.tar\.gz$/,
		'generic': /^picto-\d+\.\d+\.\d+\.zip$/
	};

	try {
		var response = await fetch(apiUrl);
		if (!response.ok) {
			throw new Error('Failed to fetch latest release.');
		}

		var release = await response.json();
		var version = document.getElementById('version');
		if (version && release.tag_name) {
			version.textContent = release.tag_name;
		}

		Object.keys(targets).forEach(function (platform) {
			var link = document.querySelector('[data-platform="' + platform + '"]');
			var asset = release.assets.find(function (item) {
				return targets[platform].test(item.name);
			});

			if (link && asset) {
				link.href = asset.browser_download_url;
			}
		});
	} catch (e) {
	}
})();
