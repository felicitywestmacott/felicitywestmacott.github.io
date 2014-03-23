---
---

var imgHeight;
var imgWidth;
var imgPath;

var timer;
var targetOp = 0;
var actualOp = 0;

function setOpacity(value) {
	el = document.getElementById("hubert");
	el.style.opacity = value;
	el.style.filter = 'alpha(opacity=' + Math.round(value * 100) + ")";
}

function fadeToTarget() {
	actualOp += (targetOp - actualOp) * 0.3;
	if (Math.abs(actualOp - targetOp) < 0.1) {
		clearInterval(timer);
		actualOp = targetOp;
	}
	setOpacity(actualOp);
}

function fadeTo(value) {

	if (targetOp == value) {
		return;
	}

	if (timer) {
		clearInterval(timer);
	}

	targetOp = value;
	timer = setInterval(fadeToTarget, 50);
}

function borderise(value) {
	border = 0.3;
	plusBorders = 1.0 + border + border;
	value = (value * plusBorders) - border;
	value = Math.max(0, Math.min(value, 1));
	return value;
}

function enbig() {
	if (document.getElementById('enbig').style.display == 'block') {
		document.getElementById('enbig').style.display = 'none';
	}
	else {
		el = document.getElementById("leftside");
		if (imgWidth >= el.offsetWidth || imgHeight >= el.offsetHeight) {
			document.getElementById('enbig').style.display = 'block';
		}
	}
}

function enlarge(event) {

  elA = document.getElementById("leftside");
  elB = document.getElementById("hubert");

	//elB.style.maxWidth = '' + imgWidth + 'px';
	//elB.style.maxHeight = '' + imgHeight + 'px';
	//elB.style.backgroundSize = imgWidth + 'px ' + imgHeight + 'px';


  ev = event || window.event;
  mouseX = ev.clientX - elA.offsetLeft;
  mouseY = ev.clientY - elA.offsetTop;

  diffX = elA.offsetWidth - elB.offsetWidth;
  diffY = elA.offsetHeight - elB.offsetHeight;

  border = 0.3;
  plusBorders = 1.0 + border + border;

  if (diffX < 0) {
    offsetProp = mouseX / elA.offsetWidth;
	offsetX = Math.round(diffX * borderise(offsetProp));
  }
  else {
    offsetX = diffX / 2;
  }
  if (diffY < 0) {
    offsetProp = mouseY / elA.offsetHeight;
	offsetY = Math.round(diffY * borderise(offsetProp));
  }
  else {
    offsetY = diffY / 2;
  }
  elB.style.left = offsetX + "px";
  elB.style.top = offsetY + "px";

  fadeTo(1.0);
}

function ensmall() {

	resizeBackground();

	el = document.getElementById("leftside");
	//el.style.backgroundRepeat='no-repeat';
	//el.style.backgroundPosition='center';
	//el.style.backgroundColor='#FFEEEE';
	//el.style.backgroundSize='contain';

	//el.style.cursor='auto';

	if (imgWidth < el.offsetWidth && imgHeight < el.offsetHeight) {
		el.style.cursor='auto';
	}
	else {
		el.style.cursor="url('{{ site.baseimageref }}graphics/mag.png') 12 12, auto";
	}

	//elA = document.getElementById("elA");
	//elB = document.getElementById("hubert");

	//elB.style.maxWidth = '' + elA.offsetWidth + 'px';
	//elB.style.maxHeight = '' + elA.offsetHeight + 'px';
	//elB.style.backgroundSize = elA.offsetWidth  + 'px ' + elA.offsetHeight + 'px';

	fadeTo(0.0);
}

function resizeBackground() {
	el = document.getElementById("leftside");
	xScale = el.offsetWidth / imgWidth;
	yScale = el.offsetHeight / imgHeight;
	scale = Math.min(xScale, yScale, 1.0);
	//el.style.backgroundImage="url('" + imgPath + "')";
	el.style.backgroundSize = (imgWidth * scale) + 'px ' + (imgHeight * scale) + 'px';
}

//window.onresize = resizeBackground();

function whenLoaded(thisImage, nextImagePath) {

	imgHeight = thisImage.height;
	imgWidth = thisImage.width;

	el = document.getElementById("leftside");
	el.style.backgroundImage="url('" + imgPath + "')";

	el = document.getElementById("hubert");
	targetOp = 0;
	actualOp = 0;
	setOpacity(0);
	el.style.backgroundImage="url('" + imgPath + "')";
	el.style.width = '' + imgWidth + 'px';
	el.style.height = '' + imgHeight + 'px';

	resizeBackground();

	ensmall();

	if (nextImagePath != "N/A") {
		var preLoadedImage = new Image();
		preLoadedImage.src = nextImagePath;
	}

	return true;
}

function loadImage(path, nextImage) {

  imgPath = path;

  var myImage = new Image();
  myImage.onload = function(event) {
    whenLoaded(myImage, nextImage);
	return true;
  };

  myImage.src = path;
}

function moveChildTo(newParent, childId) {
	var child = document.getElementById(childId);
	if (child) {
		newParent.appendChild(child);
	}
}

function uglyHack() {
    /* Firefox relative/absolute positioning issues within a table cell.
	   Breaks IE layout if hardcoded into HTML. */
	if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1) {
		var parent = document.getElementById('bobby');
		moveChildTo(parent, 'hubert');
		moveChildTo(parent, 'counter');
		moveChildTo(parent, 'next');
		moveChildTo(parent, 'prev');
		parent.style.height="100%";
	}
/*	if (navigator.appName == 'Microsoft Internet Explorer') {
		document.getElementById('leftside');
	filter: progid:DXImageTransform.Microsoft.AlphaImageLoader( src='arrow-big-right.png', sizingMethod='scale');
    -ms-filter: "progid:DXImageTransform.Microsoft.AlphaImageLoader( src='arrow-big-right.png', sizingMethod='scale')";
	}*/
}

function preparePage(path, nextImage) {
	uglyHack();
	loadImage(path, nextImage);

	document.getElementById('enbig').onclick = enbig;
	document.getElementById('leftside').onclick = enbig;

}

function stopEvent(e) {
  if ( e.stopPropagation ) e.stopPropagation();
  e.cancelBubble = true; /*  required for IE */
}