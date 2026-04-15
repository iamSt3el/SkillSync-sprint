const obsidian = require('obsidian');

class LaserPenPlugin extends obsidian.Plugin {
  settings = {
    penColor: '#FF0000',
    penWidth: 3,
    fadeTime: 2000, // Time in ms for the laser drawing to fade
  };

  async onload() {
    console.log('Loading Laser Pen plugin');

    // Add settings tab
    this.addSettingTab(new LaserPenSettingTab(this.app, this));

    // Register the laser pen view
    this.registerView(
      'laser-pen-view',
      (leaf) => new LaserPenView(leaf, this.settings)
    );

    // Add ribbon icon for toggling the laser pen
    this.addRibbonIcon('pencil', 'Toggle Laser Pen', () => {
      this.toggleLaserPen();
    });

    // Add command for toggling the laser pen
    this.addCommand({
      id: 'toggle-laser-pen',
      name: 'Toggle Laser Pen',
      callback: () => {
        this.toggleLaserPen();
      },
      hotkeys: [
        {
          modifiers: ['Ctrl'],
          key: 'L',
        },
      ],
    });
  }

  async toggleLaserPen() {
    const { workspace } = this.app;
    
    // Check if laser pen is already activated
    const existing = workspace.getLeavesOfType('laser-pen-view');
    
    if (existing.length) {
      // If active, remove it
      existing.forEach(leaf => leaf.detach());
    } else {
      // Create a floating window that acts as an overlay
      // This will create a transparent overlay on top of the current view
      // rather than creating a new empty file
      this.laserOverlay = document.createElement('div');
      this.laserOverlay.className = 'laser-pen-overlay';
      this.laserOverlay.style.position = 'fixed';
      this.laserOverlay.style.top = '0';
      this.laserOverlay.style.left = '0';
      this.laserOverlay.style.width = '100%';
      this.laserOverlay.style.height = '100%';
      this.laserOverlay.style.pointerEvents = 'none'; // Initially none, will change when Ctrl is pressed
      this.laserOverlay.style.zIndex = '9999';
      
      // Create canvas for drawing
      this.canvas = document.createElement('canvas');
      this.canvas.style.width = '100%';
      this.canvas.style.height = '100%';
      this.canvas.width = window.innerWidth;
      this.canvas.height = window.innerHeight;
      this.laserOverlay.appendChild(this.canvas);
      
      // Add to document
      document.body.appendChild(this.laserOverlay);
      
      // Initialize canvas
      this.ctx = this.canvas.getContext('2d');
      
      // Add event listeners
      this.mouseDownHandler = this.onMouseDown.bind(this);
      this.mouseMoveHandler = this.onMouseMove.bind(this);
      this.mouseUpHandler = this.onMouseUp.bind(this);
      this.resizeHandler = this.onResize.bind(this);
      this.keyDownHandler = this.onKeyDown.bind(this);
      this.keyUpHandler = this.onKeyUp.bind(this);
      
      document.addEventListener('mousedown', this.mouseDownHandler);
      document.addEventListener('mousemove', this.mouseMoveHandler);
      document.addEventListener('mouseup', this.mouseUpHandler);
      document.addEventListener('keydown', this.keyDownHandler);
      document.addEventListener('keyup', this.keyUpHandler);
      window.addEventListener('resize', this.resizeHandler);
      
      // Start rendering loop
      this.isActive = true;
      this.points = [];
      this.drawings = [];
      this.renderLoop();
      
      // Show notification
      new obsidian.Notice('Laser Pen activated. Hold Ctrl and drag to draw.');
    }
  }
  
  onKeyDown(event) {
    // When Ctrl key is pressed, enable pointer events on overlay to capture mouse
    // and prevent text selection and other Ctrl-based functionality
    if (event.key === 'Control' && this.isActive && this.laserOverlay) {
      this.laserOverlay.style.pointerEvents = 'auto';
      // Prevent default Ctrl behaviors
      event.preventDefault();
      
      // Add a class to the body to disable text selection
      document.body.classList.add('laser-pen-active');
    }
  }
  
  onKeyUp(event) {
    // When Ctrl key is released, disable pointer events on overlay
    if (event.key === 'Control' && this.isActive && this.laserOverlay) {
      this.laserOverlay.style.pointerEvents = 'none';
      
      // Remove the class from the body
      document.body.classList.remove('laser-pen-active');
      
      // End any active drawing when Ctrl is released
      if (this.isDrawing) {
        this.isDrawing = false;
        if (this.points.length > 1) {
          this.drawings.push({
            points: [...this.points],
            color: this.settings.penColor,
            width: this.settings.penWidth,
            startTime: Date.now()
          });
        }
        this.points = [];
      }
    }
  }

  onMouseDown(event) {
    // Check if active and Ctrl is pressed (which now changes pointer-events to auto)
    if (this.isActive && event.ctrlKey) {
      this.isDrawing = true;
      this.points = [{
        x: event.clientX,
        y: event.clientY,
        time: Date.now()
      }];
      event.preventDefault();
      event.stopPropagation();
    }
  }

  onMouseMove(event) {
    if (this.isDrawing) {
      this.points.push({
        x: event.clientX,
        y: event.clientY,
        time: Date.now()
      });
      event.preventDefault();
      event.stopPropagation();
    }
  }

  onMouseUp(event) {
    if (this.isDrawing) {
      this.isDrawing = false;
      // Save the current drawing
      if (this.points.length > 1) {
        this.drawings.push({
          points: [...this.points],
          color: this.settings.penColor,
          width: this.settings.penWidth,
          startTime: Date.now()
        });
      }
      this.points = [];
      event.preventDefault();
      event.stopPropagation();
    }
  }
  
  onResize() {
    if (this.canvas) {
      this.canvas.width = window.innerWidth;
      this.canvas.height = window.innerHeight;
    }
  }
  
  renderLoop() {
    if (!this.isActive || !this.ctx) return;
    
    // Clear canvas
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    
    const now = Date.now();
    
    // Draw active drawing
    if (this.isDrawing && this.points.length > 1) {
      this.ctx.beginPath();
      this.ctx.moveTo(this.points[0].x, this.points[0].y);
      
      for (let i = 1; i < this.points.length; i++) {
        this.ctx.lineTo(this.points[i].x, this.points[i].y);
      }
      
      this.ctx.strokeStyle = this.settings.penColor;
      this.ctx.lineWidth = this.settings.penWidth;
      this.ctx.lineCap = 'round';
      this.ctx.lineJoin = 'round';
      this.ctx.stroke();
    }
    
    // Draw saved drawings with fading effect
    this.drawings = this.drawings.filter(drawing => {
      const age = now - drawing.startTime;
      
      // Skip if the drawing should be completely faded
      if (age > this.settings.fadeTime) {
        return false;
      }
      
      // Calculate the progress of fading (0 to 1)
      const fadeProgress = age / this.settings.fadeTime;
      
      // Draw the line with a gradient effect
      // Instead of reducing the number of visible points, we'll draw the full line
      // with a gradient that moves along the line
      
      // We can't use a normal canvas gradient because it's based on coordinates
      // So we'll draw small segments with different opacities
      
      const totalPoints = drawing.points.length;
      if (totalPoints < 2) return true;
      
      for (let i = 0; i < totalPoints - 1; i++) {
        const pointPosition = i / (totalPoints - 1); // 0 at start, 1 at end
        
        // Calculate opacity based on position and fade progress
        // The fade boundary is where the fade effect is currently happening
        const fadeBoundary = fadeProgress;
        
        // If the point is before the fade boundary, it should be invisible
        // If it's after, it should be visible
        // We add a small transition zone for a smoother effect
        const transitionWidth = 0.3; // Width of the fade transition zone
        
        let pointOpacity;
        if (pointPosition < fadeBoundary - transitionWidth) {
          // Points well before the fade boundary are completely transparent
          pointOpacity = 0;
        } else if (pointPosition > fadeBoundary) {
          // Points after the fade boundary are fully opaque
          pointOpacity = 1;
        } else {
          // Points in the transition zone have a gradient opacity
          const transitionPosition = (pointPosition - (fadeBoundary - transitionWidth)) / transitionWidth;
          pointOpacity = Math.max(0, Math.min(1, transitionPosition));
        }
        
        // Skip drawing fully transparent segments
        if (pointOpacity <= 0) continue;
        
        // Draw the segment
        this.ctx.beginPath();
        this.ctx.moveTo(drawing.points[i].x, drawing.points[i].y);
        this.ctx.lineTo(drawing.points[i+1].x, drawing.points[i+1].y);
        
        this.ctx.strokeStyle = this.hexToRgba(drawing.color, pointOpacity);
        this.ctx.lineWidth = drawing.width;
        this.ctx.lineCap = 'round';
        this.ctx.lineJoin = 'round';
        this.ctx.stroke();
      }
      
      return true;
    });
    
    // Continue the render loop
    requestAnimationFrame(this.renderLoop.bind(this));
  }

  hexToRgba(hex, opacity) {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${opacity})`;
  }

  async onload() {
    console.log('Loading Laser Pen plugin');

    // Add settings tab
    this.addSettingTab(new LaserPenSettingTab(this.app, this));

    // Register the laser pen view (legacy support)
    this.registerView(
      'laser-pen-view',
      (leaf) => new LaserPenView(leaf, this.settings)
    );

    // Add ribbon icon for toggling the laser pen
    this.addRibbonIcon('pencil', 'Toggle Laser Pen', () => {
      this.toggleLaserPen();
    });

    // Add command for toggling the laser pen
    this.addCommand({
      id: 'toggle-laser-pen',
      name: 'Toggle Laser Pen',
      callback: () => {
        this.toggleLaserPen();
      },
      hotkeys: [
        {
          modifiers: ['Ctrl'],
          key: 'L',
        },
      ],
    });
    
    // Add CSS to disable text selection when laser pen is active
    const css = `
      body.laser-pen-active {
        user-select: none !important;
        cursor: crosshair !important;
      }
      body.laser-pen-active ::selection {
        background: transparent !important;
      }
      body.laser-pen-active * {
        cursor: crosshair !important;
      }
    `;
    
    const styleEl = document.createElement('style');
    styleEl.textContent = css;
    document.head.appendChild(styleEl);
    this.styleEl = styleEl;
  }

  async onunload() {
    console.log('Unloading Laser Pen plugin');
    
    // Clean up overlay if it exists
    if (this.laserOverlay) {
      document.body.removeChild(this.laserOverlay);
      this.laserOverlay = null;
    }
    
    // Remove style element
    if (this.styleEl) {
      document.head.removeChild(this.styleEl);
    }
    
    // Remove the class from body if it's there
    document.body.classList.remove('laser-pen-active');
    
    // Remove event listeners
    if (this.mouseDownHandler) {
      document.removeEventListener('mousedown', this.mouseDownHandler);
    }
    if (this.mouseMoveHandler) {
      document.removeEventListener('mousemove', this.mouseMoveHandler);
    }
    if (this.mouseUpHandler) {
      document.removeEventListener('mouseup', this.mouseUpHandler);
    }
    if (this.keyDownHandler) {
      document.removeEventListener('keydown', this.keyDownHandler);
    }
    if (this.keyUpHandler) {
      document.removeEventListener('keyup', this.keyUpHandler);
    }
    if (this.resizeHandler) {
      window.removeEventListener('resize', this.resizeHandler);
    }
    
    this.isActive = false;
    
    // Clean up by removing any active laser pen views (legacy cleanup)
    this.app.workspace.getLeavesOfType('laser-pen-view').forEach(leaf => leaf.detach());
  }
}

class LaserPenView extends obsidian.ItemView {
  constructor(leaf, settings) {
    super(leaf);
    this.settings = settings;
    this.isDrawing = false;
    this.points = [];
    this.drawings = [];
  }

  getViewType() {
    return 'laser-pen-view';
  }

  getDisplayText() {
    return 'Laser Pen';
  }

  async onOpen() {
    const container = this.containerEl.children[1];
    container.empty();
    container.style.position = 'absolute';
    container.style.top = '0';
    container.style.left = '0';
    container.style.width = '100%';
    container.style.height = '100%';
    container.style.pointerEvents = 'none';
    container.style.zIndex = '1000';

    // Create canvas for drawing
    this.canvas = document.createElement('canvas');
    this.canvas.style.width = '100%';
    this.canvas.style.height = '100%';
    this.canvas.style.position = 'absolute';
    this.canvas.style.top = '0';
    this.canvas.style.left = '0';
    this.canvas.style.pointerEvents = 'none';
    container.appendChild(this.canvas);

    // Initialize canvas
    this.ctx = this.canvas.getContext('2d');
    this.resizeCanvas();

    // Add event listeners
    this.registerDomEvent(document, 'mousedown', this.onMouseDown.bind(this));
    this.registerDomEvent(document, 'mousemove', this.onMouseMove.bind(this));
    this.registerDomEvent(document, 'mouseup', this.onMouseUp.bind(this));
    this.registerDomEvent(window, 'resize', this.resizeCanvas.bind(this));

    // Start rendering loop
    this.renderLoop();
  }

  resizeCanvas() {
    this.canvas.width = window.innerWidth;
    this.canvas.height = window.innerHeight;
  }

  onMouseDown(event) {
    // Only activate when Ctrl key is pressed
    if (event.ctrlKey) {
      this.isDrawing = true;
      this.points = [{
        x: event.clientX,
        y: event.clientY,
        time: Date.now()
      }];
    }
  }

  onMouseMove(event) {
    if (this.isDrawing && event.ctrlKey) {
      this.points.push({
        x: event.clientX,
        y: event.clientY,
        time: Date.now()
      });
    }
  }

  onMouseUp(event) {
    if (this.isDrawing) {
      this.isDrawing = false;
      // Save the current drawing
      if (this.points.length > 1) {
        this.drawings.push({
          points: [...this.points],
          color: this.settings.penColor,
          width: this.settings.penWidth,
          startTime: Date.now()
        });
      }
      this.points = [];
    }
  }

  renderLoop() {
    // Clear canvas
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    
    const now = Date.now();
    
    // Draw active drawing
    if (this.isDrawing && this.points.length > 1) {
      this.ctx.beginPath();
      this.ctx.moveTo(this.points[0].x, this.points[0].y);
      
      for (let i = 1; i < this.points.length; i++) {
        this.ctx.lineTo(this.points[i].x, this.points[i].y);
      }
      
      this.ctx.strokeStyle = this.settings.penColor;
      this.ctx.lineWidth = this.settings.penWidth;
      this.ctx.lineCap = 'round';
      this.ctx.lineJoin = 'round';
      this.ctx.stroke();
    }
    
    // Draw saved drawings with fading effect
    this.drawings = this.drawings.filter(drawing => {
      const age = now - drawing.startTime;
      
      // Skip if the drawing should be completely faded
      if (age > this.settings.fadeTime) {
        return false;
      }
      
      // Calculate opacity based on age
      const opacity = 1 - (age / this.settings.fadeTime);
      
      // Draw the line with reduced opacity
      this.ctx.beginPath();
      this.ctx.moveTo(drawing.points[0].x, drawing.points[0].y);
      
      for (let i = 1; i < drawing.points.length; i++) {
        this.ctx.lineTo(drawing.points[i].x, drawing.points[i].y);
      }
      
      this.ctx.strokeStyle = this.hexToRgba(drawing.color, opacity);
      this.ctx.lineWidth = drawing.width;
      this.ctx.lineCap = 'round';
      this.ctx.lineJoin = 'round';
      this.ctx.stroke();
      
      return true;
    });
    
    // Continue the render loop
    requestAnimationFrame(this.renderLoop.bind(this));
  }

  hexToRgba(hex, opacity) {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${opacity})`;
  }

  async onClose() {
    // Clean up
  }
}

class LaserPenSettingTab extends obsidian.PluginSettingTab {
  constructor(app, plugin) {
    super(app, plugin);
    this.plugin = plugin;
  }

  display() {
    const { containerEl } = this;
    containerEl.empty();

    new obsidian.Setting(containerEl)
      .setName('Pen Color')
      .setDesc('Color of the laser pen')
      .addColorPicker(color => color
        .setValue(this.plugin.settings.penColor)
        .onChange(async (value) => {
          this.plugin.settings.penColor = value;
          await this.plugin.saveSettings();
        }));

    new obsidian.Setting(containerEl)
      .setName('Pen Width')
      .setDesc('Width of the laser pen stroke')
      .addSlider(slider => slider
        .setLimits(1, 10, 1)
        .setValue(this.plugin.settings.penWidth)
        .setDynamicTooltip()
        .onChange(async (value) => {
          this.plugin.settings.penWidth = value;
          await this.plugin.saveSettings();
        }));

    new obsidian.Setting(containerEl)
      .setName('Fade Time')
      .setDesc('Time in seconds for the laser drawing to fade away')
      .addSlider(slider => slider
        .setLimits(0.5, 10, 0.5)
        .setValue(this.plugin.settings.fadeTime / 1000)
        .setDynamicTooltip()
        .onChange(async (value) => {
          this.plugin.settings.fadeTime = value * 1000;
          await this.plugin.saveSettings();
        }));
  }
}

module.exports = LaserPenPlugin;
