var scrollAmount;

function getWidth() {
  var myWidth = 0;
  if( typeof( window.innerWidth ) == 'number' ) {
    //Non-IE
    myWidth = window.innerWidth;
  } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
    //IE 6+ in 'standards compliant mode'
    myWidth = document.documentElement.clientWidth;
  } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
    //IE 4 compatible
    myWidth = document.body.clientWidth;
  }
  return myWidth;
}

function pageScroll() {
  scrollAmount = scrollAmount * 0.95;
  if (scrollAmount < 2 && scrollAmount > -2) {
    return;
  }
  document.getElementById('scrollable').scrollLeft += scrollAmount;
  setTimeout('pageScroll()',10);
}

function scrollLeftwards() {
  scrollAmount=(160-getWidth()) / 18;
  pageScroll();
}

function scrollRightwards() {
  scrollAmount=(getWidth() - 80) / 18;
  pageScroll();
}

///////////////////////////////////////////////////////////

/* As yet unused. */

function getParameterByName(name) {

    var match = RegExp('[?&]' + name + '=([^&]*)')
                    .exec(window.location.search);

    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));

}

///////////////////////////////////////////////////////////

function filterBy(aclassname) {

	var allSpans=document.getElementsByClassName('filterable');
	for (i=0; i<allSpans.length; i++) {

			if (allSpans[i].className.match(aclassname)) {
				allSpans[i].style.display='inline-block';
			}
			else {
				allSpans[i].style.display='none';
			}

	}
}

////////////////////////////////////////////////////////////////////

var scrollAmount2 = 0;
var dScrollAmount2 = 1;
var scrollDirection = 0;
var intervalId;
var intervalSet = 0;

function findBottom(obj) {
	var curtop = 336;
	if (obj.offsetParent) {
    	do {
			curtop += obj.offsetTop;
		} while (obj = obj.offsetParent);
	}
	return curtop;
}

function findTop(obj) {
	var curtop = 0;
	if (obj.offsetParent) {
    	do {
			curtop += obj.offsetTop;
		} while (obj = obj.offsetParent);
	}
	return curtop;
}

function stopScroll() {
	dScrollAmount2 = 0.9;
}

function setScrollAmount(event) {

	var scrollPortion = 1/4;
	var ramp = 5;
	var curve = 4;

	var w = getWidth();
	var ev = event || window.event;
	var x = ev.pageX;
	var y = ev.pageY;
	var proximity = scrollPortion - Math.min(x, w - x) / w;

	if (proximity <= 0 || y > findBottom(document.getElementById('scrollable')) || y < findTop(document.getElementById('scrollable'))) {
		stopScroll();
		return;
	}
	else {
		dScrollAmount2 = 0.95;
	}

	var speed = Math.pow((1 + (ramp * proximity)), curve);

	if (x > w/2) {
		scrollAmount2 = speed;
	}
	else {
		scrollAmount2 = -speed;
	}
	if (intervalSet == 0) {
		intervalId = setInterval('doScrollAmount()',30);
		intervalSet = 1;
	}
}

function doScrollAmount() {
	scrollAmount2 = scrollAmount2 * dScrollAmount2;
	if (Math.abs(scrollAmount2) < 0.5) {
		clearInterval(intervalId);
		intervalSet = 0;
	}
	document.getElementById('scrollable').scrollLeft += scrollAmount2;
}


////////////////////////////////////////////////////////////////////

function handle(delta) {

	scrollAmount=(delta * -10);
	pageScroll();

}

function wheel(event){
	var delta = 0;
	if (!event) event = window.event;
	if (event.wheelDelta) {
		delta = event.wheelDelta/120;
	} else if (event.detail) {
		delta = -event.detail/3;
	}
	if (delta)
		handle(delta);
    if (event.preventDefault)
        event.preventDefault();
    event.returnValue = false;
}

///////////////////////////////////////////////////////////////////////

function getInternetExplorerVersion() {
  var rv = -1;
  if (navigator.appName == 'Microsoft Internet Explorer')
  {
    var ua = navigator.userAgent;
    var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if (re.exec(ua) != null)
      rv = parseFloat( RegExp.$1 );
  }
  return rv;
}

function initialise() {

	filterBy('all');

	var ver = getInternetExplorerVersion();

	if (ver > -1 && ver < 9) {
	  return;
	}

	/* Initialization code. */
	if (window.addEventListener)
		window.addEventListener('DOMMouseScroll', wheel, false);
	window.onmousewheel = document.onmousewheel = wheel;

	document.onmousemove = setScrollAmount;
	document.onmouseout = stopScroll;
}