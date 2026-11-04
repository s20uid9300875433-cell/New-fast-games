# Hexa Color Quest (Android)

A native Android 2.5D hexagon slide-and-merge puzzle game built with **Kotlin** and **Jetpack Compose** (Material Design 3), featuring 100 progressive levels across 5 chapters, smooth drag-and-drop and tap interactions, cascade combos, procedural audio synthesis, tactile haptics, and offline local progress persistence.

## Features

- **100 Progressive Levels Across 5 Chapters**: Handcrafted onboarding stages (`Ocean Pearl` through `The Coral Nexus`) followed by deterministic strategic puzzles (`Normal`, `Hard`, `Hard+`, `Very Hard`, `Expert`, and `Master`) with radius-2 (19-cell) and radius-3 (37-cell) boards and irregular obstacle layouts.
- **Dual Placement & Slide Mechanics**: Drag or tap blocks from the 3-slot Color Block Tray onto open sockets, or tap and slide existing board tiles to adjacent empty neighbors.
- **6 Glossy 2.5D Tile Tiers & Cascade Combos**: Synthesize 3+ connected matching tiles from **Ruby Heart (T1)** through **Amber Star (T2)**, **Emerald Clover (T3)**, **Azure Hexa (T4)**, **Violet Moon (T5)**, up to **Prismatic Crown (T6)** with automatic multi-step chain reactions and radial sparkle bursts.
- **Smart Hint Solver & Full Undo**: Built-in move solver highlights optimal tray placements or board slides, alongside full multi-step undo history.
- **Offline Procedural Audio & Tactile Haptics**: Real-time PCM `AudioTrack` synthesizer for harmonic merge chimes and ambient Lydian background chords, paired with Android `Vibrator` haptics and local DataStore progress storage.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3, Custom 2.5D `Canvas` Renderer, Adaptive Layouts
- **Architecture**: MVVM (`AppViewModel`, `GameViewModel`) + Repository Pattern
- **Navigation**: Jetpack Navigation Compose (type-safe `@Serializable` routes)
- **Persistence**: Jetpack DataStore Preferences + `kotlinx.serialization`
