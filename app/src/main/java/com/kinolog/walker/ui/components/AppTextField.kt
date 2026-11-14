package com.kinolog.walker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinolog.walker.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.extraBottomMarginOnFocus(extraBottom: Dp = 96.dp): Modifier = composed {
    val density = LocalDensity.current
    val extraBottomPx = with(density) { extraBottom.toPx() }
    val responder = remember(extraBottomPx) {
        object : BringIntoViewResponder {
            override fun calculateRectForParent(localRect: Rect): Rect {
                return localRect.copy(bottom = localRect.bottom + extraBottomPx)
            }

            override suspend fun bringChildIntoView(localRect: () -> Rect?) {
                // Родительский verticalScroll сам плавно доскроллит до нужной позиции
                // с учётом calculateRectForParent, повторный скролл не требуется.
                return
            }
        }
    }
    this.bringIntoViewResponder(responder)
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bringIntoViewOnFocus(extraBottom: Dp = 96.dp): Modifier = extraBottomMarginOnFocus(extraBottom)

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    containerColor: Color? = null,
    extraBottom: Dp = 96.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isDark = LocalIsDarkTheme.current
    val defaultContainer = Color.Transparent
    val fieldContainerColor = containerColor ?: defaultContainer
    val disabledContainer = containerColor ?: Color.Transparent

    val colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDark) Color(0xFFEAEFEA) else Color(0xFF1A1D1B),
        unfocusedTextColor = if (isDark) Color(0xFFEAEFEA) else Color(0xFF1A1D1B),
        disabledTextColor = if (isDark) Color(0xFF627067) else Color(0xFF98A39C),
        errorTextColor = if (isDark) Color(0xFFEAEFEA) else Color(0xFF1A1D1B),
        focusedContainerColor = fieldContainerColor,
        unfocusedContainerColor = fieldContainerColor,
        disabledContainerColor = disabledContainer,
        errorContainerColor = fieldContainerColor,
        cursorColor = if (isDark) Color(0xFF68B684) else Color(0xFF4A8267),
        errorCursorColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFD9534F),
        focusedBorderColor = if (isDark) Color(0xFF68B684) else Color(0xFF4A8267),
        unfocusedBorderColor = if (isDark) Color(0xFF34463B) else Color(0xFFCAD7CF),
        disabledBorderColor = if (isDark) Color(0xFF222C25) else Color(0xFFE0DCD6),
        errorBorderColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFD9534F),
        focusedLabelColor = if (isDark) Color(0xFF68B684) else Color(0xFF4A8267),
        unfocusedLabelColor = if (isDark) Color(0xFFA0ABA4) else Color(0xFF68706B),
        disabledLabelColor = if (isDark) Color(0xFF627067) else Color(0xFF98A39C),
        errorLabelColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFD9534F),
        focusedPlaceholderColor = if (isDark) Color(0xFF7A887F) else Color(0xFF98A39C),
        unfocusedPlaceholderColor = if (isDark) Color(0xFF7A887F) else Color(0xFF98A39C),
        disabledPlaceholderColor = if (isDark) Color(0xFF4B564E) else Color(0xFFB5BEB8),
        focusedPrefixColor = if (isDark) Color(0xFFEAEFEA) else Color(0xFF1A1D1B),
        unfocusedPrefixColor = if (isDark) Color(0xFFEAEFEA) else Color(0xFF1A1D1B),
        focusedSuffixColor = if (isDark) Color(0xFFEAEFEA) else Color(0xFF1A1D1B),
        unfocusedSuffixColor = if (isDark) Color(0xFFEAEFEA) else Color(0xFF1A1D1B),
        errorSupportingTextColor = if (isDark) Color(0xFFEF9A9A) else Color(0xFFD9534F)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.extraBottomMarginOnFocus(extraBottom),
        label = label?.let {
            {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        readOnly = readOnly,
        enabled = enabled,
        shape = shape,
        colors = colors,
        interactionSource = interactionSource,
        textStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    )
}
