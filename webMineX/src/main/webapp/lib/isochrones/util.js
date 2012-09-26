function IsoUtil() {
}

/**
 * Converts km per hour in meters per second
 * 
 * @param {number}
 *          speed value in km per hours
 * @return {number} the value in meters per second
 */
IsoUtil.kmp2mps = function(speed) {
	return parseFloat(speed) / 3.666;
}

/**
 * Converts minutes in seconds
 * 
 * @param {number}
 *          minutes
 * @return {number} seconds
 */
IsoUtil.minutes2seconds = function(minutes) {
	return parseInt(minutes) * 60;
}

/**
 * 
 * @param {Date}
 *          the date instance to be converted
 * @return {String} the string value in pattern: YYYY/MM/DD:hh24:mi
 */
IsoUtil.date2String = function(date) {
	var sep = "/"
	var dateString = date.getFullYear() + sep;
	dateString += IsoUtil.fillZeroPrefix(date.getMonth() + 1) + sep;
	dateString += IsoUtil.fillZeroPrefix(date.getDate()) + sep;
	dateString += IsoUtil.fillZeroPrefix(date.getHours()) + ":";
	dateString += IsoUtil.fillZeroPrefix(date.getMinutes());
	return dateString;
}

/**
 * 
 * @param {Object}
 *          dateString
 */
IsoUtil.string2Date = function(dateString) {
	return new Date(dateString);
}

/**
 * 
 * @param {Date}
 *          date
 * @return {String} the date string in format YYYY-MM-DD
 */
IsoUtil.date2DayString = function(date) {
	var sep = "-";
	var dateString = IsoUtil.fillZeroPrefix(date.getDate()) + sep;
	dateString += IsoUtil.fillZeroPrefix(date.getMonth() + 1) + sep;
	dateString += IsoUtil.fillZeroPrefix(date.getFullYear());
	return dateString;
}

IsoUtil.asHTML = function(id, distance, distanceInTime) {
	var s = "<html><style type=\"text/css\">table.featureInfo,table.featureInfo td,table.featureInfo th { ";
	s += "border: 1px solid #ddd;border-collapse: collapse;	margin: 0;padding: 0;font-size: 90%;padding: .2em .1em;} ";
	s += "table.featureInfo th {padding: .2em .2em;	font-weight: bold;	background: #eee;} ";
	s += "table.featureInfo td {background: #fff;} ";
	s += "table.featureInfo tr.odd td \{	background: #eee;\} ";
	s += "table.featureInfo caption {	text-align: left; font-size: 100%; font-weight: bold; text-transform: uppercase; padding: .2em .2em;}";
	s += "a:link {text-decoration:none;}	a:visited {text-decoration:none;} a:hover {text-decoration:underline;} a:active {text-decoration:underline;}";
	s += "</style>";
	s += "<body><table class=\"featureInfo\" id=\"popupTable\"><tr><th>Attribute</th><th>Value</th></tr>";
	s += "<tr><td>Id</td><td>" + id + "</td></tr>";
	s += "<tr><td>Distance (sec)</td><td>" + distance + "</td></tr>";
	s += "<tr><td>Distance (time)</td><td>" + distanceInTime + "</td></tr>";
	s += "<tr><td id=\"loadSchedulesCell\"colspan=\"2\"><a id=\"loadSchedulesLink\" onclick=\"loadSchedules(" + id
			+ ")\">Show schedule</a></td></tr>";
	s += "</table></body></html>";
	return s;
}

/**
 * 
 * @param {Date}
 *          date
 * @return {String} the time string in format hh:mm
 */
IsoUtil.date2TimeString = function(date) {
	var timeString = IsoUtil.fillZeroPrefix(date.getHours()) + ":";
	timeString += IsoUtil.fillZeroPrefix(date.getMinutes());
	return timeString;
}

/**
 * 
 * @param {String}
 *          date the date string in pattern DD/MM/YYYY
 * @param {String}
 *          time the time string in pattern hh:mm
 * @return {Date} the date instance
 */
IsoUtil.mergeDate = function(date, time) {
	var result;
	result = date.substring(6, 10) + "/" + date.substring(3, 5) + "/" + date.substring(0, 2) + "/" + time;
	return new Date(result);
}

/**
 * 
 * @param {Date}
 *          dateObj the date object
 * @param {integer}
 *          seconds the seconds to be subtracted
 */
IsoUtil.subtractSeconds = function(dateObj, seconds) {
	var d = new Date(dateObj.getTime());
	if (seconds) {
		d.setSeconds(-seconds);
	}
	var timeString = IsoUtil.fillZeroPrefix(d.getHours());
	timeString += ":" + IsoUtil.fillZeroPrefix(d.getMinutes());
	timeString += ":" + IsoUtil.fillZeroPrefix(d.getSeconds());
	return timeString
}

/**
 * 
 * @param {Date}
 *          dateObj the date object
 * @param {integer}
 *          seconds the seconds to be subtracted
 */
IsoUtil.addSeconds = function(dateObj, seconds) {
	var d = new Date(dateObj.getTime());
	if (seconds) {
		d.setSeconds(seconds);
	}
	var timeString = IsoUtil.fillZeroPrefix(d.getHours());
	timeString += ":" + IsoUtil.fillZeroPrefix(d.getMinutes());
	timeString += ":" + IsoUtil.fillZeroPrefix(d.getSeconds());
	return timeString
}

/**
 * 
 * @param {Date}
 *          the date instance
 * @param {Object}
 *          feature the feature from which to extract the information
 * @return {String} a div element containing the information to be displayed
 */
IsoUtil.toHtml = function(time, feature) {
	var htmlToReturn = "<div style='font-size:0.8em;overflow:hidden'>"
	htmlToReturn += "<img src='img/clock.png' width='15' height='15' alt='bus'/> <b>";
	if (!feature) {
		htmlToReturn += IsoUtil.subtractSeconds(time, 0);
	} else {
		htmlToReturn += IsoUtil.subtractSeconds(time, feature.data.distance);
		htmlToReturn += "</b>";

		for (i = 0; i < feature.data.routes.length; i++) {
			var routeAnns = feature.data.routes[i];
			var routeType = routeAnns.routeType == null ? 1 : routeAnns.routeType;
			var routeSymbol = imgLookup[routeType];
			var tokens = routeAnns.routeName.split(" ");
			var routeName = "";
			for (j = 0; j < tokens.length; j++) {
				var token = tokens[j];
				if (j < tokens.length - 1) {
					routeName += token;
				} else {
					if (token == "BZ" || token == "ME") {
						; // do not add it
					} else {
						routeName += token;
					}
				}
			}
			htmlToReturn += " <img src='" + routeSymbol + "' width='15' height='15' alt='bus'/> " + routeName;
		}
	}
	htmlToReturn += "</div>";
	return htmlToReturn;
}

/**
 * 
 * @param {String}
 *          dateString date string with pattern: YYYY-MM-DD[-hh:mm]
 * @return {Date} the date instance
 * 
 * IsoUtil.toDate = function (dateString){ var parts = dateString.split('-');
 * var year = parts[0]; var month = parts[1]; var day = parts[2]; var hour =
 * parts[3]; var min = parts[4]; var myDate; if (!hour) { myDate = new
 * Date(year, month, day); } else { myDate = new Date(year, month, day, hour,
 * min); } return myDate; }
 */
/**
 * 
 * @param {Number}
 *          the numeric value
 * @return {String} the value in string format with leading zero in case of 1
 *         digit
 */
IsoUtil.fillZeroPrefix = function(val) {
	if (val < 10)
		return "0" + val;
	else
		return val;

}

IsoUtil.asId = function(featureId) {
	// remember: each regex has to start and end with a / character
	var id = featureId.replace(/[\w]*\./,"");
	return parseInt(id);
}
