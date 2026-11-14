package com.kinolog.walker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinolog.walker.model.PetItem
import com.kinolog.walker.ui.theme.CharcoalPrimary
import com.kinolog.walker.ui.theme.LocalIsDarkTheme
import com.kinolog.walker.ui.theme.MintLight
import com.kinolog.walker.ui.theme.SoftMint

@Composable
fun PetTag(
    pet: PetItem,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val breedColor = if (isDark) Color(0xFFD2DDD6) else Color(0xFF555F5A)

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MintLight)
            .border(0.5.dp, SoftMint, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${pet.type.icon} ${pet.name}",
            color = CharcoalPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        if (pet.breedOrDetails.isNotBlank()) {
            Text(
                text = " • ${pet.breedOrDetails}",
                color = breedColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
