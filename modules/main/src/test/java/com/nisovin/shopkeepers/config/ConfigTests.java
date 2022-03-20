package com.nisovin.shopkeepers.config;

import static com.nisovin.shopkeepers.testutil.matchers.IsDataFuzzyEqual.dataFuzzyEqualTo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Assert;
import org.junit.Test;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.testutil.AbstractBukkitTest;
import com.nisovin.shopkeepers.util.bukkit.ConfigUtils;
import com.nisovin.shopkeepers.util.data.container.DataContainer;
import com.nisovin.shopkeepers.util.java.ClassUtils;

public class ConfigTests extends AbstractBukkitTest {

	private DataContainer loadConfigFromResource(String resourcePath) {
		InputStream configResource = ClassUtils.getResource(this.getClass(), resourcePath);
		Configuration config = YamlConfiguration.loadConfiguration(new InputStreamReader(configResource));
		// In order to be able to compare the contents of this loaded config with the data of an
		// in-memory serialized config, we need to convert all config sections to Maps.
		// Modifiable shallow copy:
		Map<@NonNull String, @NonNull Object> configData = ConfigUtils.getValues(config);
		ConfigUtils.convertSectionsToMaps(configData);
		return DataContainer.ofNonNull(configData); // Map-based data container
	}

	@Test
	public void testDefaultConfigConsistency() {
		// Expected default config:
		DataContainer expectedDefaultConfig = DataContainer.create();
		Settings.getInstance().save(expectedDefaultConfig);
		Set<? extends @NonNull String> expectedKeysSet = expectedDefaultConfig.getKeys();
		List<? extends @NonNull String> expectedKeys = new ArrayList<>(expectedKeysSet);
		Map<? extends @NonNull String, @NonNull ?> expectedValues = expectedDefaultConfig.getValues();

		// Actual default config:
		DataContainer defaultConfig = this.loadConfigFromResource("config.yml");
		Set<? extends @NonNull String> actualKeysSet = defaultConfig.getKeys();
		List<? extends @NonNull String> actualKeys = new ArrayList<>(actualKeysSet);
		Map<? extends @NonNull String, @NonNull ?> actualValues = defaultConfig.getValues();

		// Check for missing keys:
		for (String expectedKey : expectedKeys) {
			Assert.assertTrue("The default config is missing the key '" + expectedKey + "'!",
					actualKeysSet.contains(expectedKey));

			// Compare values:
			Object expectedValue = expectedValues.get(expectedKey);
			Object actualValue = actualValues.get(expectedKey);
			Assert.assertThat("The value for key '" + expectedKey
					+ "' of the default config does not match the expected value!",
					actualValue, dataFuzzyEqualTo(expectedValue));
		}

		// Check for unexpected keys:
		for (String actualKey : actualKeys) {
			Assert.assertTrue("The default config contains the unexpected key '" + actualKey + "'!",
					expectedKeysSet.contains(actualKey));
		}

		// Check the order of keys and for duplicated keys:
		Assert.assertEquals("The default config keys do not match the expected keys!",
				expectedKeys, actualKeys);
	}

	@Test
	public void testDefaultLanguageFilesConsistency() {
		// Expected default language file:
		DataContainer expectedDefaultLanguageFile = DataContainer.create();
		Messages.getInstance().save(expectedDefaultLanguageFile);
		Set<? extends @NonNull String> expectedKeysSet = expectedDefaultLanguageFile.getKeys();
		List<? extends @NonNull String> expectedKeys = new ArrayList<>(expectedKeysSet);
		Map<? extends @NonNull String, @NonNull ?> expectedValues = expectedDefaultLanguageFile.getValues();

		// Actual default language files:
		@NonNull String[] languageFilePaths = new @NonNull String[] {
				"lang/language-en-default.yml",
				"lang/language-de.yml"
		};
		for (String languageFilePath : languageFilePaths) {
			DataContainer languageFile = this.loadConfigFromResource(languageFilePath);
			Set<? extends @NonNull String> actualKeysSet = languageFile.getKeys();
			List<? extends @NonNull String> actualKeys = new ArrayList<>(actualKeysSet);

			// Check for missing keys:
			for (String expectedKey : expectedKeys) {
				Assert.assertTrue("The default language file '" + languageFilePath
						+ "' is missing the key '" + expectedKey + "'!",
						actualKeysSet.contains(expectedKey));
			}

			// Check for unexpected keys:
			for (String actualKey : actualKeys) {
				Assert.assertTrue("The default language file '" + languageFilePath
						+ "' contains the unexpected key '" + actualKey + "'!",
						expectedKeysSet.contains(actualKey));
			}

			// Check the order of keys and for duplicated keys:
			Assert.assertEquals("The keys of the default language file '" + languageFilePath
					+ "' do not match the expected keys!",
					expectedKeys, actualKeys);

			if (languageFilePath.equals("lang/language-en-default.yml")) {
				// Compare values:
				Map<? extends @NonNull String, @NonNull ?> actualValues = languageFile.getValues();

				// Compare values:
				for (String expectedKey : expectedKeys) {
					Object expectedValue = expectedValues.get(expectedKey);
					Object actualValue = actualValues.get(expectedKey);
					Assert.assertEquals("The value for key '" + expectedKey
							+ "' of the default language file '" + languageFilePath
							+ "' does not match the expected value!",
							Unsafe.nullableAsNonNull(expectedValue),
							Unsafe.nullableAsNonNull(actualValue)
					);
				}
			}
		}
	}
}
