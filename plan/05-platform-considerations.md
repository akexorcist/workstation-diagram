# Platform-Specific Considerations

## Web Platform (Compose for Web)

**File:** `jsMain/kotlin/Main.kt`

### Browser Compatibility

- **Target Browsers:**
  - Chrome (latest)
  - Firefox (latest)
  - Safari (latest)
  - Edge (latest)

- **Testing Requirements:**
  - Test on all target browsers
  - Handle browser-specific rendering differences
  - Test canvas performance
  - Test touch gestures (if supported)

### Web-Specific Features

1. **URL Parameters:**
   - Support URL parameters for initial state
   - Example: `?device=laptop-office&zoom=1.5`
   - Parse parameters on load
   - Update URL on state changes (optional)

2. **Browser Navigation:**
   - Support browser back/forward navigation
   - Maintain state in URL or session storage
   - Handle page refresh gracefully

3. **Responsive Design:**
   - Adapt to different screen sizes
   - Support mobile browsers (if needed)
   - Handle window resizing
   - Touch gestures for mobile (pinch to zoom, drag to pan)

4. **Performance:**
   - Code splitting for faster load
   - Lazy loading of large diagrams
   - Web Workers for heavy calculations (if needed)
   - Optimize bundle size

### Web Implementation Details

**Entry Point:**
```kotlin
fun main() {
    renderComposable(rootElementId = "root") {
        WorkstationDiagramApp()
    }
}
```

**HTML Integration:**
- Create HTML file with root div
- Load Kotlin/JS bundle
- Handle page lifecycle

**Resource Loading:**
- Load JSON data from resources
- Handle CORS if loading from external sources
- Cache data in browser storage

### Web-Specific Optimizations

1. **Bundle Size:**
   - Minimize dependencies
   - Use tree-shaking
   - Code splitting by route/feature

2. **Rendering Performance:**
   - Optimize canvas rendering
   - Use requestAnimationFrame
   - Debounce resize events

3. **Memory Management:**
   - Clean up event listeners
   - Dispose of resources properly
   - Handle memory leaks

---

## Desktop Platform (Compose Desktop)

**File:** `desktopMain/kotlin/Main.kt`

### Window Management

1. **Window Configuration:**
   - Initial window size (e.g., 1280x720)
   - Minimum window size
   - Window title
   - Window icon

2. **Window State Persistence:**
   - Save window size/position
   - Restore on next launch
   - Handle multi-monitor setups
   - Remember maximized state

3. **Window Features:**
   - Fullscreen mode support
   - Window resizing handling
   - Multi-window support (if needed)
   - Window menu integration

### File System

1. **Data Loading:**
   - Load data from file system
   - Support file picker for loading different layouts
   - Handle file read errors
   - Support drag-and-drop for data files

2. **File Operations:**
   - Open file dialog
   - Save file dialog (for export)
   - Recent files list
   - File associations (optional)

### Native Features

1. **System Integration:**
   - System menu integration
   - Native keyboard shortcuts
   - Native look and feel
   - System notifications (optional)

2. **Platform-Specific Features:**
   - macOS: Menu bar, native controls
   - Windows: Taskbar integration, native dialogs
   - Linux: Desktop integration, native file dialogs

### Desktop Implementation Details

**Entry Point:**
```kotlin
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Workstation Diagram",
        state = rememberWindowState(size = DpSize(1280.dp, 720.dp))
    ) {
        WorkstationDiagramApp()
    }
}
```

**Window State Management:**
- Use `rememberWindowState` for window configuration
- Save/restore window state
- Handle window events

**File Dialogs:**
- Use Compose Desktop file dialogs
- Filter by JSON files
- Handle file selection

### Desktop-Specific Optimizations

1. **Performance:**
   - Native rendering performance
   - Hardware acceleration
   - Efficient memory usage

2. **User Experience:**
   - Native file dialogs
   - Native keyboard shortcuts
   - System integration
   - Better performance than web

---

## Shared Code Considerations

### Platform-Agnostic Code

- **Maximum Code Sharing:**
  - All business logic in `commonMain`
  - All UI in `composeMain`
  - Platform-specific only for entry points

- **Platform Detection:**
  - Use `expect/actual` for platform differences
  - Minimize platform-specific code
  - Test shared code on both platforms

### Common Patterns

1. **Resource Loading:**
   - Web: Load from resources or URL
   - Desktop: Load from resources or file system
   - Use common interface

2. **Storage:**
   - Web: Use localStorage/sessionStorage
   - Desktop: Use local file system
   - Abstract storage behind interface

3. **Preferences:**
   - Web: Store in localStorage
   - Desktop: Store in local file
   - Use common preferences interface

---

## Platform-Specific Testing

### Web Testing

1. **Browser Testing:**
   - Test on all target browsers
   - Test different screen sizes
   - Test touch interactions
   - Test performance

2. **Web-Specific Tests:**
   - URL parameter parsing
   - Browser navigation
   - Resource loading
   - Storage operations

### Desktop Testing

1. **Platform Testing:**
   - Test on macOS
   - Test on Windows
   - Test on Linux
   - Test native features

2. **Desktop-Specific Tests:**
   - File system operations
   - Window management
   - Native dialogs
   - System integration

---

## Performance Considerations

### Web Performance

1. **Initial Load:**
   - Minimize bundle size
   - Lazy load components
   - Optimize images/assets

2. **Runtime Performance:**
   - Optimize canvas rendering
   - Use efficient algorithms
   - Cache calculations
   - Debounce events

3. **Memory:**
   - Clean up resources
   - Avoid memory leaks
   - Monitor memory usage

### Desktop Performance

1. **Rendering:**
   - Native rendering performance
   - Hardware acceleration
   - Efficient updates

2. **Memory:**
   - Efficient memory usage
   - Proper resource cleanup
   - Monitor memory usage

---

## Deployment Considerations

### Web Deployment

1. **Build:**
   - Build Kotlin/JS bundle
   - Optimize bundle size
   - Generate HTML

2. **Hosting:**
   - Static file hosting
   - CDN for assets
   - HTTPS support

3. **Distribution:**
   - GitHub Pages
   - Netlify/Vercel
   - Custom hosting

### Desktop Deployment

1. **Build:**
   - Build native executables
   - Package for each platform
   - Include resources

2. **Distribution:**
   - macOS: DMG or App Bundle
   - Windows: MSI or EXE installer
   - Linux: AppImage or DEB/RPM

3. **Signing:**
   - Code signing for macOS
   - Code signing for Windows
   - GPG signing for Linux

---

## Platform-Specific Features Summary

### Web Only
- URL parameters
- Browser navigation
- Browser storage (localStorage)
- Responsive design for mobile
- Code splitting

### Desktop Only
- File system access
- Native file dialogs
- Window state persistence
- System menu integration
- Native keyboard shortcuts
- Multi-window support

### Both Platforms
- Canvas rendering
- Zoom/pan functionality
- Theme switching
- Device selection
- Connection rendering
- All business logic

---

## Migration Between Platforms

### Code Sharing Strategy

1. **Common Code:**
   - All data models
   - All domain logic
   - All UI components (Compose)
   - All algorithms

2. **Platform-Specific:**
   - Entry points only
   - File system access
   - Storage implementation
   - Native features

### Testing Strategy

1. **Shared Tests:**
   - Unit tests for common code
   - UI tests for Compose components
   - Algorithm tests

2. **Platform Tests:**
   - Platform-specific features
   - Integration tests
   - End-to-end tests

