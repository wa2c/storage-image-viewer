package com.wa2c.android.storageimageviewer.presentation.ui.common.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.wa2c.android.storageimageviewer.presentation.ui.common.theme.AppColor

@Composable
fun DividerWide() = HorizontalDivider(thickness = 2.dp, color = AppColor.Divider)

@Composable
fun DividerNormal() = HorizontalDivider(thickness = 1.dp, color = AppColor.Divider)

@Composable
fun DividerThin() = HorizontalDivider(thickness = 0.5.dp, color = AppColor.Divider)
