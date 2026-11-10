package com.pt2vhf.aprs.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * #72: campo de busca compacto. O OutlinedTextField do Material 3 precisa de 56 dp; forçado a 48 dp ele cortava
 * a base do texto. Aqui o preenchimento interno é menor e a altura só tem mínimo, então o texto digitado e o
 * placeholder aparecem inteiros mesmo com fonte grande de acessibilidade.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = 44.dp),
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, color = Pal.text),
        cursorBrush = SolidColor(Pal.accent),
        interactionSource = interaction,
        decorationBox = { inner ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = inner,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interaction,
                placeholder = { Text(placeholder, fontSize = 14.sp, maxLines = 1) },
                colors = colors,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            )
        },
    )
}
