function RepulsionField() {}

RepulsionField.prototype.alterParticle = function(particleIndex, particles, elapsedMs) {
	var i, 
		fx = 0, fy = 0,
		inRepulseZone = 0,
		dx, dy, d2,
		n = particles.length,
		particle = particles[particleIndex], 
		other,
		repulsionRadius  = 10*particle.radius,
		repulsionRadius2 = repulsionRadius * repulsionRadius;

	for(i=0; i<n; i++) {
		if(i!==particleIndex) {
			other = particles[i];

			dx = particle.x - other.x,
			dy = particle.y - other.y,
			d2 = dx*dx + dy*dy;

			if(d2 < repulsionRadius2) {
				fx += 0.02 * dx / d2;
				fy += 0.02 * dy / d2;
				inRepulseZone++;
			}
		}
	}

	particle.fx += fx;
	particle.fy += fx;
	particle.colorIndex = inRepulseZone;
}