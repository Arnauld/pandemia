var PI2 = Math.PI / 2;
var COLORS = [	["rgba(10, 10, 10, 0.2)"], 
				["rgba(0, 125,  0, 0.2)"], 
				["rgba(0, 255, 0, 0.2)"], 
				["rgba(125, 125, 0, 0.2)"], 
				["rgba(125, 0, 0, 0.2)"], 
				["rgba(255, 0, 0, 0.2)"]];


function Particle(x,y,angle,speed) {
	this.x = x;
	this.y = y;
	this.angle = angle;
	this.speed = speed;
	this.radius = 3;
	this.repulsionRadius  = 10*this.radius;
	this.fill = true;
	this.px = -1;
	this.py = -1;
	this.shape = "triangle";
	this.colorIndex = 0;
	this.fx = 0;
	this.fy = 0;
}

Particle.prototype.resetForce = function() {
	this.fx = 0;
	this.fy = 0;
}

Particle.prototype.update = function(elapsedMs) {
	var speed = this.speed,
    	angle = this.angle,
    	vx = speed * Math.cos(angle),
		vy = speed * Math.sin(angle);

	vx += this.fx * elapsedMs;
	vy += this.fy * elapsedMs;

	this.px  = this.x + vx * elapsedMs;
	this.py  = this.y + vy * elapsedMs;	
}

Particle.prototype.checkBounds = function(xmin, ymin, xmax, ymax) {
	if(this.px < xmin) {
		this.px = xmin + (xmin - this.px);
		this.angle = Math.PI - this.angle;
	}
	else if(this.px > xmax) {
		this.px = xmax - (this.px - xmax);
		this.angle = Math.PI - this.angle;
	}

	if(this.py < ymin) {
		this.py = ymin + (ymin - this.py);
		this.angle = - this.angle;
	}
	else if(this.py > ymax) {
		this.py = ymax - (this.py - ymax);
		this.angle = - this.angle;
	}
}

Particle.prototype.draw = function(context) {
	if(this.px>=0) {
		this.x = this.px;
		this.y = this.py;
		this.px = -1;
		this.py = -1;
	}
	var rgb = COLORS[Math.min(this.colorIndex, COLORS.length - 1)];

	context.save();
   	context.translate(this.x, this.y);

	//Background 360 degree arc
	context.beginPath();
	context.lineWidth = 1;
	context.fillStyle = rgb[0];
	context.arc(0, 0, this.repulsionRadius, 0, Math.PI*2, false); //you can see the arc now
	context.stroke();
	if(this.fill) {
		context.fill();
	}

	context.beginPath();
	context.lineWidth = 1;
	context.fillStyle = "rgba(0, 0, 0, 1.0)";
	if(this.shape === "circle") {
		context.arc(0, 0, this.radius, 0, Math.PI*2, false); //you can see the arc now
	}
	else {
		context.rotate(this.angle - PI2);
		context.moveTo(0, 2*this.radius);
		context.lineTo(-this.radius * 0.5, -this.radius);
		context.lineTo(+this.radius * 0.5, -this.radius);
		context.lineTo(0, this.radius);
	}
	context.stroke();
	if(this.fill) {
		context.fill();
	}

	context.restore();
}

function ParticleSystem(options) {
	options = options || {};
	var i,
		count = options.count || 20,
		H = options.height || 300,
		W = options.width  || 300,
		speedMax = options.speedMax || 0.2,
		particles = []
		updaters = options.updaters || [ new RepulsionField() ];

	for(i=0; i<count; i++) {
		var x  = W * Math.random(),
			y  = W * Math.random(),
			a  = 2*Math.PI * Math.random(),
			v  = speedMax * Math.random(),
			p  = new Particle(x,y,a,v);
		particles.push(p);
	}

	this.updaters = updaters;
	this.particles = particles;
	this.H = H;
	this.W = W;
}

ParticleSystem.prototype.update = function(context, elapsedMs) {
	this.updateParticles(elapsedMs);
	this.drawParticles(context);
}

ParticleSystem.prototype.updateParticles = function(elapsedMs) {
	var updaters = this.updaters,
		nu = updaters.length;

	if(nu !== 0) {
		var particles = this.particles,
			n = particles.length,
			particle, i;
		for(i=0; i<n; i++) {
			particle = particles[i];
			particle.resetForce();
			for(j=0; j<nu; j++) {
				updaters[j].alterParticle(i, particles, elapsedMs);
			}
			particle.update(elapsedMs);
			particle.checkBounds(0, 0, this.W, this.H);
		}		
	}
}

ParticleSystem.prototype.drawParticles = function(context) {
	var particles = this.particles,
			n = particles.length,
			i;
	for(i=0; i<n; i++) {
		particles[i].draw(context);
	}
}
