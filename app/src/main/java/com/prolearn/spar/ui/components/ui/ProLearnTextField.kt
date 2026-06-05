package com.prolearn.spar.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import com.prolearn.spar.ui.theme.ProLearnShapes

@Composable
fun ProLearnTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    isError: Boolean = false
) {
    val borderColor = when {
        isError -> ProLearnColors.Error
        else -> ProLearnColors.Border
    }
    val visualTransformation = if (isPassword && !showPassword)
        PasswordVisualTransformation() else VisualTransformation.None

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(ProLearnShapes.md)
            .background(if (isError) ProLearnColors.ErrorSurface else ProLearnColors.White)
            .border(1.5.dp, borderColor, ProLearnShapes.md),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = BricolageGrotesqueFamily,
                    color = ProLearnColors.Black
                ),
                singleLine = true,
                cursorBrush = SolidColor(ProLearnColors.Black),
                visualTransformation = visualTransformation,
                keyboardOptions = if (isPassword)
                    KeyboardOptions(keyboardType = KeyboardType.Password)
                else
                    KeyboardOptions.Default,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 15.sp,
                                fontFamily = BricolageGrotesqueFamily,
                                color = ProLearnColors.Muted
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (isPassword && onTogglePassword != null) {
                IconButton(
                    onClick = onTogglePassword,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = ProLearnColors.Muted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
