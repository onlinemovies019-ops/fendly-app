package com.example.fendly.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fendly.R

@Composable
@JvmName("FendlyLogo")
fun FendlyLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 48.sp,
    @Suppress("UNUSED_PARAMETER") iconSize: Dp = 28.dp,
) {
    Image(
        painter = painterResource(id = R.drawable.fendly_logo),
        contentDescription = "Fendly",
        modifier = modifier
            .width((fontSize.value * 5.2f).dp)
            .semantics { text = AnnotatedString("Fendly") },
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
    )
}
