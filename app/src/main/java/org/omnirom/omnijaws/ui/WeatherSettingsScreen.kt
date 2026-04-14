/*
 * Copyright 2025 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnirom.omnijaws.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.ListPreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.scaffold.AxionScaffold
import org.omnirom.omnijaws.R

@Composable
fun WeatherSettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onEnableChanged: (Boolean) -> Unit,
    onProviderChanged: (String) -> Unit,
    onUnitsChanged: (String) -> Unit,
    onIntervalChanged: (String) -> Unit,
    onCustomLocationChanged: (Boolean) -> Unit,
    onLocationPickerClick: () -> Unit,
    onIconPackChanged: (String) -> Unit,
    onOwmKeyChanged: (String) -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    val intervalValues = stringArrayResource(R.array.update_interval_options_values)
    val intervalLabels = stringArrayResource(R.array.update_interval_options_labels)
    val options = intervalValues.zip(intervalLabels)

    AxionScaffold(
        title = stringResource(R.string.weather_settings_title),
        onBackClick = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PreferenceGroup {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.enable_weather_service_title),
                        checked = state.enabled,
                        onCheckedChange = onEnableChanged,
                        icon = Icons.Outlined.Cloud
                    )
                }
            }

            if (state.enabled) {
                PreferenceGroup(title = stringResource(R.string.general_title)) {
                    item {
                        ListPreference(
                            title = stringResource(R.string.weather_provider_title),
                            summary = state.providerLabel,
                            options = listOf("0" to "OpenWeatherMap", "1" to "MET Norway"),
                            value = state.provider,
                            onValueChange = onProviderChanged
                        )
                    }
                    item {
                        ListPreference(
                            title = stringResource(R.string.temperature_unit_title),
                            summary = state.unitsLabel,
                            options = listOf("0" to "Metric (\u00b0C)", "1" to "Imperial (\u00b0F)"),
                            value = state.units,
                            onValueChange = onUnitsChanged
                        )
                    }
                    item {
                        ListPreference(
                            title = stringResource(R.string.update_interval_title),
                            summary = state.intervalLabel,
                            options = options,
                            value = state.updateInterval,
                            onValueChange = onIntervalChanged
                        )
                    }
                    item {
                        ClickablePreference(
                            title = stringResource(R.string.last_update_title),
                            summary = state.lastUpdateTime.ifEmpty { "Never" },
                            icon = Icons.Outlined.Update,
                            onClick = {}
                        )
                    }
                }

                PreferenceGroup(title = stringResource(R.string.location_title)) {
                    item {
                        SwitchPreference(
                            title = stringResource(R.string.custom_location_title),
                            summary = stringResource(R.string.custom_location_summary),
                            checked = state.customLocation,
                            onCheckedChange = onCustomLocationChanged,
                            icon = Icons.Outlined.MyLocation
                        )
                    }
                    if (state.customLocation) {
                        item {
                            ClickablePreference(
                                title = stringResource(R.string.custom_location_title),
                                summary = stringResource(R.string.not_set_summary),
                                icon = Icons.Outlined.LocationOn,
                                onClick = onLocationPickerClick
                            )
                        }
                    }
                    if (!state.customLocation && !state.hasLocationPermission) {
                        item {
                            ClickablePreference(
                                title = stringResource(R.string.grant_location_permission_title),
                                summary = stringResource(R.string.grant_location_permission_summary),
                                icon = Icons.Outlined.Security,
                                onClick = onRequestLocationPermission
                            )
                        }
                    }
                }

                if (state.iconPacks.isNotEmpty()) {
                    PreferenceGroup(title = stringResource(R.string.appearance_title)) {
                        item {
                            ListPreference(
                                title = stringResource(R.string.icon_pack_title),
                                summary = state.iconPacks.firstOrNull { it.value == state.iconPack }?.label,
                                options = state.iconPacks.map { it.value to it.label },
                                value = state.iconPack,
                                onValueChange = onIconPackChanged
                            )
                        }
                    }
                }

                if (state.provider == "0") {
                    PreferenceGroup(title = "API") {
                        item {
                            EditTextPreference(
                                title = "OpenWeatherMap API key",
                                value = state.owmKey,
                                onValueChange = onOwmKeyChanged
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditTextPreference(
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var textValue by remember(value) { mutableStateOf(value) }

    ClickablePreference(
        title = title,
        summary = value.ifEmpty { "Not set" },
        icon = Icons.Outlined.Key,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(textValue)
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
