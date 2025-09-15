## Snappy Ruler Set — Android (Kotlin + Jetpack Compose)

This app provides a drawing canvas with virtual geometry tools that snap intelligently for fast, accurate construction. Built with Kotlin and Jetpack Compose Canvas. Core logic is UI-agnostic and cleanly separated from rendering.

### Architecture Overview
- **Layers**
  - **Render state**: Immutable models of shapes, grid, and tool poses (e.g., `LineSegment`, `Arc`, `ToolPose`). Central `DrawingState` is the single source of truth for what to render.
  - **Interaction state**: Gesture-derived transient state (active pointers, hover/snap candidates, selection). Drives tool manipulation and drawing but is not persisted.
  - **Persistence**: Serialization of `DrawingState` for undo/redo history and export.
- **Core modules**
  - **Geometry**: Math utilities (angles, projections, intersections, circle/line ops).
  - **Snapping**: Strategy that orchestrates grid/point/angle candidates with a spatial index and dynamic snap radius.
  - **Tools**: Ruler, Set Square (45°, 30–60°), Protractor, Compass (optional). Each exposes hit-test, drag/rotate, and edge/ray generation APIs.
  - **Undo/Redo**: Command stack capturing atomic edits to `DrawingState` (≥ 20 steps). Gesture micro-moves are coalesced per gesture.
  - **Rendering**: Compose Canvas layer to draw shapes, tools, precision HUD, and snap affordances.

### Snapping Strategy & Data Structures
- **Snap types**
  - **Grid**: Configurable spacing (default 5 mm). Grid points generated on-demand within viewport.
  - **Points**: Endpoints, midpoints, circle centers, intersections cached in a spatial index.
  - **Angles**: Common angles {0, 30, 45, 60, 90, 120, 135, 150, 180}° and nearest 1° for readouts.
- **Dynamic radius**: Radius scales with zoom: larger when zoomed out, smaller when zoomed in. Example: `radiusPx = clamp(8, 24) * (baseZoom / zoom)`.
- **Selection policy**: Rank by distance and salience: explicit user targets > existing geometry points > grid. Break ties by smallest distance; show chosen candidate with a tick + highlight. Provide subtle haptic on hard snaps.
- **Spatial index**: Uniform grid buckets (screen-space bins) or an R-tree over points/segments/arcs. Lookup candidates in bins overlapping the snap radius.

### Interactions & UX
- **Gestures**: One-finger pan, two-finger pinch zoom, two-finger rotate selected tool, long-press to temporarily toggle snap.
- **Drawing**: Freehand pen; edge-aligned drawing along tool edges with snapping to grid/points/angles.
- **Protractor**: Place on vertex, measure angle between rays; readout snaps to nearest 1° with hard stops at common angles.
- **Precision HUD**: Small overlay for current length (cm/mm) and angle (°).
- **Feedback**: Magnetic visual hint (tick + highlight) and gentle haptic on snap.

### Performance Notes
- **Target**: 60 fps during tool manipulation on mid-range devices.
- **Approach**:
  - Immutable render state; redraw minimal regions.
  - Cache geometry-derived points; update spatial index incrementally.
  - Avoid allocations in hot paths; reuse objects and math buffers.
  - Throttle hit-testing to frame cadence; prefer screen-space binning.
- **Expected**: Frame times under ~16 ms during pan/zoom/rotate; snapping queries under ~1 ms with binning.

### Calibration & Units
- **Default**: If unknown, 160 dpi ≈ 1 dp ≈ 1 px for length display.
- **Preferred**: Use `DisplayMetrics.xdpi/ydpi` for device-based calibration.
- **User calibration**: Optional flow to align on a known object length (e.g., credit card) and store factor.
- **Display**: Lengths in cm with 1 mm granularity.

### Accuracy Targets
- **Lengths**: 1 mm granularity (HUD), using calibrated dpi for cm conversion.
- **Angles**: Readouts within ±0.5°. Snap to nearest 1°; hard snaps at common angles.

### Undo/Redo
- Command-pattern history with at least 20 steps. Coalesce gesture deltas until gesture end.



