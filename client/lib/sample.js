(function() {
	"use strict";

	//canvas initialization
	var canvas  = document.getElementById("canvas"),
		context = canvas.getContext("2d"),
	//dimensions
		W = canvas.width,
		H = canvas.height,
		previousTs = Date.now();


	// animated objects
	var animates = [];

	// draw all
	var redraw = function() {
		//Clear the canvas everytime a chart is drawn
		context.clearRect(0, 0, W, H);

		var i, 
			len = animates.length,
			now = Date.now();

		for(i=0; i<len; i++) {
			animates[i].update(context, now - previousTs);
		}
		previousTs = now;

		setTimeout(redraw, 1000/60); // 60 frames/secondes?
	}

	var particles = new ParticleSystem({count:20, width:W, height:H});
	animates.push(particles);
	redraw();
})();