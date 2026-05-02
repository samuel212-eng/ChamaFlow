package com.chamaflow.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Digital Blue & Minimalist White Palette
val Primary      = Color(0xFF0052FF) // Vibrant Digital Blue
val PrimaryDark  = Color(0xFF003EB3)
val Secondary    = Color(0xFF3B82F6) // Sky Blue
val Accent       = Color(0xFF10B981) // Success Green
val Background   = Color(0xFFF9FAFB) // Minimal Grey
val Surface      = Color(0xFFFFFFFF) // Pure White
val Error        = Color(0xFFEF4444)
val Warning      = Color(0xFFF59E0B)

// Refined Text Colors
val TextPrimary   = Color(0xFF111827) // Dark Grey-Black
val TextSecondary = Color(0xFF4B5563) // Medium Grey
val TextMuted     = Color(0xFF9CA3AF) // Light Grey

// Legacy Compatibility Aliases
val ChamaBlue         = Primary
val ChamaBlueLight    = Color(0xFFEFF6FF)
val ChamaBlueDark     = PrimaryDark
val ChamaGreen        = Accent
val ChamaGreenLight   = Color(0xFFECFDF5)
val ChamaGreenDark    = Color(0xFF065F46)
val ChamaGold         = Warning
val ChamaGoldLight    = Color(0xFFFFFBEB)
val ChamaRed          = Error
val ChamaRedLight     = Color(0xFFFEF2F2)
val ChamaOrange       = Color(0xFFF97316)
val ChamaOrangeLight  = Color(0xFFFFF7ED)
val ChamaBackground   = Background
val ChamaSurface      = Surface
val ChamaOutline      = Color(0xFFE5E7EB)
val ChamaTextPrimary   = TextPrimary
val ChamaTextSecondary = TextSecondary
val ChamaTextMuted     = TextMuted

// Modern Gradients
val PremiumGradient = listOf(Color(0xFF0052FF), Color(0xFF003EB3))
val WhiteGradient   = listOf(Color(0xFFFFFFFF), Color(0xFFF3F4F6))
val GlassGradient   = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.4f))
