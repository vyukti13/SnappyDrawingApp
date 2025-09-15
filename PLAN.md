## Implementation Plan — Snappy Ruler Set

### Overview (no timelines)
- Establish Kotlin + Jetpack Compose Canvas foundation with a unidirectional data flow separating render state, interaction state, and persistence.
- Build core geometry/math utilities (vectors, angles, intersections, projections) and unit conversion with optional user calibration.
- Define state models for shapes, tools, and viewport; implement gesture handling for pan, zoom, rotation, and snap toggling.
- Implement grid rendering and freehand drawing, with a Precision HUD for lengths/angles.
- Create snapping infrastructure with a spatial index, dynamic snap radius, candidate generation (grid, endpoints, midpoints, centers, intersections), and selection policy with visual/haptic feedback.
- Implement tools incrementally: Ruler (translate/rotate, edge-aligned drawing, angle/point snapping), Set Square variants (45°, 30–60°) with edge-aligned drawing, and Protractor (place on vertex, measure rays, 1° readout with hard snaps). Optionally add Compass (center/radius snapping, arcs/circles).
- Add undo/redo using a command-pattern history, coalescing gesture deltas until gesture end, ensuring at least 20 steps.
- Implement export by rendering the current drawing state to a bitmap and invoking the share sheet, with optional grid background.
- Optimize performance for 60 fps by caching, minimizing allocations in hot paths, and throttling hit-tests to frame cadence; profile and adjust.
- Polish UX: clear snap affordances, conflict resolution visuals, subtle haptics on hard snaps, and a long-press snap toggle.
- Test geometry and snapping helpers; perform manual QA across zoom levels and device classes; record the demo video.

### Phase 0 — Project Setup 
- Initialize core modules: geometry, snapping, tools, rendering, history.
- Use Jetpack Compose Canvas as the UI layer (Kotlin only).
- Add basic app scaffold and empty canvas screen.

### Phase 1 — Core Geometry & Utilities
- Implement vector/point ops, angles, projections, intersections.
- Implement units conversion with calibration support using `DisplayMetrics` and user override.
- Add tests for angle calculations, intersection (line-line, line-circle), projections.

### Phase 2 — Render State & Interaction State
- Define `DrawingState` (lines, arcs, points), `ToolPose`, `Viewport` (pan/zoom/rotation if any).
- Define `InteractionState` (active pointers, selected tool, drag mode, snap candidates, snap enabled flag).
- Implement a unidirectional flow: gestures -> reducers -> state -> render.

### Phase 3 — Canvas, Grid, and Freehand
- Implement pan (one finger), pinch-zoom (two fingers), and render grid with configurable spacing (default 5 mm).
- Implement freehand pen path drawing; store as polylines; simplify if needed.
- Add Precision HUD showing current cursor position, length while dragging.

### Phase 4 — Snapping Infrastructure
- Implement spatial index (uniform screen-space bins or R-tree) for points/segments/arcs.
- Generate snap candidates: grid points (in-view), endpoints, midpoints, circle centers, intersections.
- Implement dynamic snap radius scaling with zoom and selection policy (distance + salience).
- Add visual affordances: tick + highlight; gentle haptic feedback on hard snap.

### Phase 5 — Ruler Tool
- Implement `RulerTool`: drag/translate, two-finger rotate, pose persistence.
- Edge-aligned drawing: draw straight lines along ruler edge with snapping to angles (0/30/45/60/90) and points.
- Angle snapping selector: nearest allowed angle unless user long-press toggles snap off.

### Phase 6 — Set Square
- Implement `SetSquareTool` variants (45°, 30–60°). Provide edges as rays/segments.
- Edge-aligned drawing with snapping to grid and existing segments.
- Interaction parity with Ruler (translate/rotate, snap toggle, HUD updates).

### Phase 7 — Protractor
- Implement `ProtractorTool`: place over vertex; define two rays.
- Measure angle between rays; readout snaps to nearest 1° with hard stops at common angles.
- Visual ticks every 1°, emphasized at 10°, hard-stop angles highlighted.

### Phase 8 — Compass (Optional)
- Implement `CompassTool`: set radius by dragging; center can snap to points/intersections.
- Draw arcs/circles; snap radius to existing distances if near.

### Phase 9 — Undo/Redo & History 
- Implement command-pattern history; coalesce gesture deltas until gesture end.
- Ensure ≥ 20 steps retained; add undo/redo UI actions.

### Phase 10 — Export & Persistence 
- Render `DrawingState` to bitmap; add share sheet (PNG/JPEG) with optional grid.
- Persist recent drawings; restore last session.

### Phase 11 — Performance Polish
- Profile frame time; ensure ≤ 16 ms per frame during interaction.
- Cache heavy computations; precompute midpoints/intersections incrementally.
- Avoid allocations in per-frame paths; pool objects.

### Phase 12 — UX Polish 
- Snap visual hierarchy when candidates compete; show reason (iconography for grid/endpoint/midpoint/intersection).
- Haptic feedback only on hard snap transitions.
- Long-press toggles snap; overlay badge while disabled.

### Phase 13 — Testing & QA
- Unit tests for geometry helpers and snapping selector.
- Manual test matrix: panning/zooming/rotating tools, snapping at various zooms, export correctness.
- Record 2–3 minute demo video per deliverables.

### Milestones & Checkpoints
- M1: Grid + pan/zoom + freehand + HUD
- M2: Snapping infra + Ruler edge-aligned drawing
- M3: Set Square variants
- M4: Protractor measurements
- M5: Undo/redo + Export
- M6: Optional Compass + Final polish
