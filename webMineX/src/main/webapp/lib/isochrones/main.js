// include util.js
var scriptNode = document.createElement('script');
scriptNode.type = 'text/javascript';
scriptNode.src = "./lib/isochrones/util.js";
document.getElementsByTagName("head")[0].appendChild(scriptNode);

// final global variables
var BL_GOOGLE = "Google maps";
var BL_OSM = "Open street maps";
var BL_PROV_OF_2011 = "Orthophoto 2011";
var BL_PROV_TRAILS = "Trails";
var BL_OFFLINE = "Offline";
var QUERYPOINT_NAME = "Query point";

var FEATURE_CONTAINER = "Feature Container";
var SELECTOR_NAME = "Selector";
var CTRL_DRAW_FEATURE = "Draw Feature";
var CTRL_SELECT_FEATURE = "Select Feature";
var CTRL_DRAG_QPOINT_FEATURE = "Drag QPointFeature";
var CLIENT_EPSG = "EPSG:3857";
var BUS_SYMBOL_URL = "img/bus.png";
var TRAIN_SYMBOL_URL = "img/train.png";
var GONDOLA_SYMBOL_URL = "img/gondola.png";
var FUNICULAR_SYMBOL_URL = "img/funicular.png";
var TYPE_PED = "ped";
var TYPE_TRAM = "tram";
var TYPE_TRAIN = "train";
var TYPE_BUS = "bus";
var TYPE_FERRY = "ferry";
var TYPE_CABLE_CAR = "cablecar";
var TYPE_GONDOLA = "gondola";
var TYPE_FUNICULAR = "funicular";
var TYPE_DEFAULT = TYPE_BUS;

var MAPPING = {
	TYPE_PED : 0,
	TYPE_TRAM : 1,
	TYPE_TRAIN : 2,
	TYPE_BUS : 3,
	TYPE_FERRY : 4,
	TYPE_CABLE_CAR : 5,
	TYPE_GONDOLA : 6,
	TYPE_FUNICULAR : 7
};

var imgLookup = {
	0 : "./img/ped.png",
	1 : "./img/tram.png",
	2 : "./img/train.png",
	3 : "./img/bus.png",
	4 : "./img/ferry.png",
	5 : "./img/cablecar.png",
	6 : "./img/gondola.png",
	7 : "./img/funicular.png"
};

// contains all the available tabs in the page
var tabPaneStatus = {
	"#helpTabPane" : false,
	"#isoTabPane" : false
}

/**
 * "#aboutTabMenu" : {"pane" : "aboutTabPane", visible:false}, "#helpTabMenu" :
 * {"pane" : "helpTabPane", visible:false}, "#isoTabMenu" : {"pane" :
 * "isoTabPane", visible:false}, "#resultTabMenu" : {"pane" : "resultTabPane",
 * visible:false}
 */

// global variables
var map;
var globalLayers = new Object();
var globalControls = new Object();
var datasetCfgs = new Object();
var cfg = new Object();
var multipleQueryPointSelection = false;
var tArrival;
var url;

var popupFeature;

OpenLayers.Feature.Vector.style['default'].cursor = 'pointer';

/**
 * function is initially invoked
 * 
 * @param {Object} $
 */
(function($) {
	var cometd = $.cometd;
	$(document).ready(function() {
		var _connected = false;
		function _connectionSucceeded() {
			OpenLayers.Console.log("CometD Connection Established");
		}

		function _connectionBroken() {
			OpenLayers.Console.log("CometD Connection Broken");
		}

		function _connectionClosed() {
			OpenLayers.Console.log("CometD Connection Closed");
		}

		// Disconnect when the page unloads
		$(window).unload(function() {
			cometd.disconnect(true);
		});

		url = location.protocol + "//" + location.host + config.contextPath + "/";

		var cometURL = url + "cometd";
		cometd.configure({
			url : cometURL,
			logLevel : 'info',
			maxNetworkDelay : 80000
		});

		/**
		 * 
		 * @param {Object}
		 *          handshake
		 */
		var handshake_ = cometd.addListener('/meta/handshake', function(handshake) {
			if (handshake.successful === true) {
				// cometd.batch(function(){
				var cfgService = cometd.subscribe('/service/cfg', function(message) {
					var dataSet = jQuery.parseJSON(message.data.dataset);
					// var
					// dataSet =
					// jQuery.parseJSON(message.data.dataset);
					for ( var i = 0; i < dataSet.length; i++) {
						var dSet = dataSet[i];
						var cfgData = {
							queryPoint : new OpenLayers.Geometry.Point(dSet.queryPoint.x, dSet.queryPoint.y),
							bbox : new OpenLayers.Bounds(dSet.bbox[0], dSet.bbox[1], dSet.bbox[2], dSet.bbox[3]),
							arrivalTime : dSet.arrivalTime,
							isoEdgeLayer : dSet.isoEdgeLayer,
							isoVertexLayer : dSet.isoVertexLayer,
							isoAreaBufferLayer : dSet.isoAreaBufferLayer,
							prefix: dSet.prefix
						}
						if (dSet.totalInhabitants) {
							cfgData["totalInhabitants"] = dSet.totalInhabitants;
						}
						datasetCfgs[dSet.name] = cfgData;
						// sets
						// the
						// value
						// to
						// the
						// checked
						// field
						var fieldDS = OpenLayers.Util.getElement(dSet.name);
						if (fieldDS.checked) {
							updatePoiField(cfgData.queryPoint);
							updateTArrivalField(cfgData.arrivalTime);
						}
					}
					config = {
						mapserverUrl : message.data.mapserverUrl
					};
					cometd.unsubscribe(cfgService);
					init();
				});
				// Publish on a service channel
				// since the message is for the
				// server only
				cometd.publish('/service/cfg', {
					"dataset" : getDataSet()
				});
				// });
			}
		});

		var connect_ = cometd.addListener('/meta/connect', function(connect) {
			if (cometd.isDisconnected()) {
				_connected = false;
				_connectionClosed();
				return;
			}

			var wasConnected = _connected;
			_connected = connect.successful === true;
			if (!wasConnected && _connected) {
				_connectionSucceeded();
			} else if (wasConnected && !_connected) {
				_connectionBroken();
			}
		});

		var disconnect_ = cometd.addListener('/meta/disconnect', function(disconnect) {
			if (disconnect.successful) {
				_connected = false;
			}
		});

		cometd.handshake();

		// event handler when tabMenu is clicked
		$("#isoTabMenu").click(function() {
			showTabMenu("#isoTabPane");
			return false;
		});

		$("#helpTabMenu").click(function() {
			showTabMenu("#helpTabPane");
			return false;
		});

	});

	$("input[name=multipleSelection]").click(function() {
		alert("selected");
	});
})(jQuery);

function changeQPointSelectionMode() {
	if ($("input[name=multipleSelection]").attr('checked')) {
		// change to multiple queryPoint selection
		$("#queryPointField").attr('disabled', true);
		multipleQueryPointSelection = true;
	} else {
		$("#queryPointField").attr('disabled', false);
		multipleQueryPointSelection = false;
	}
}

/**
 * Function that specifies if for a specific dataset the area creatin can be
 * done
 */
function enableCoverageMode() {
	var dSet;
	var dataSets = $("input[name=dataset]");
	for ( var i = 0; i < dataSets.length; i++) {
		var currentSet = dataSets[i];
		if (currentSet.checked) {
			dSet = currentSet.value;
			break;
		}
	}
	switch (dSet) {
	case "BZ":
		if ($("input[name=enableCoverage]").attr('checked')) {
			// change to multiple queryPoint selection
			$("#queryBufferDistance").attr('disabled', false);
		} else {
			$("#queryBufferDistance").attr('disabled', true);
		}
		break;
	default:
		alert("For the dataset " + dSet + " no inhabitants data are available");
		$("input[name=enableCoverage]").attr('checked', false);
	}
}

function getDataSet() {
	var dset = OpenLayers.Util.getElement("isoForm")["dataset"];
	var paraDset;
	for ( var i = 0; i < dset.length; i++) {
		if (i == 0) {
			paraDset = dset[i].value;
		} else {
			paraDset = paraDset + "," + dset[i].value;
		}
	}
	return paraDset;
}

var _connected = false;
function _metaConnect(message) {
	if (cometd.isDisconnected()) {
		_connected = false;
		_connectionClosed();
		return;
	}

	var wasConnected = _connected;
	_connected = message.successful === true;
	if (!wasConnected && _connected) {
		_connectionEstablished();
	} else if (wasConnected && !_connected) {
		_connectionBroken();
	}
}

function _connectionClosed() {
	OpenLayers.Console.log("CometD Connection Closed");
}

function _connectionEstablished() {
	OpenLayers.Console.log("CometD Connection Established");
}

function _connectionBroken() {
	OpenLayers.Console.log("CometD Connection Broken");
}

function init() {

	OpenLayers.ProxyHost = "proxy?targetURL=";
	showProgressBar();
	initBaseLayers();
	$(document).ready(function() {
		$("#progressBar").progressbar();
		$('#referenceTime').datetimepicker({
			showOn : "button",
			buttonImage : "img/cal.gif",
			buttonImageOnly : true,
			onClose : function(dateText, inst) {
				tArrival = IsoUtil.string2Date(dateText);
			}
		});
	});
	hideProgressBar();
}

function initBaseLayers() {

	var mapOptions = {
		units : 'm',
		projection : new OpenLayers.Projection(CLIENT_EPSG),
		maxExtent : new OpenLayers.Bounds(1253707.4734024, 5847459.4827448, 1275085.1962508, 5870994.5989693),
		//restrictedExtent: new OpenLayers.Bounds(0.002789244055747986,5009377.088486556,2504688.545637898,7514065.63133521),
		displayProjection: new OpenLayers.Projection("EPSG:4326"),
		resolutions:[
	     156543.0339280410,78271.51696402048,
	     39135.75848201023,19567.87924100512,
	     9783.939620502561,4891.969810251280,
				2445.98490512564,1222.992452562820,
				611.4962262814100,305.7481131407048,
				152.8740565703525,76.43702828517624,
				38.21851414258813,19.10925707129406,
				9.554628535647032,4.777314267823516,
				2.388657133911758,1.194328566955879,
				0.5971642834779395,0.29858214173896975,
				0.149291070869484875],
	};


	map = new OpenLayers.Map('map', mapOptions);

	var baseLayerGoogle = new OpenLayers.Layer.Google(BL_GOOGLE, {
		"sphericalMercator" : true,
		minZoomLevel : 1,
		maxZoomLevel : 19
	});

	var baseLayerOpenStreet = new OpenLayers.Layer.OSM(BL_OSM);
/*
	var baseLayerOrthofoto2011 = new OpenLayers.Layer.WMS(BL_PROV_OF_2011,
			"http://sdi.provinz.bz.it/geoserver/gwc/service/wms/", {
				layers : "WMTS-GMC_OF2011",
				format : "image/png8",
				projection : mapOptions.projection,
				tileOrigin : new OpenLayers.LonLat(-20037508.34, -20037508.34),
				tileSize : new OpenLayers.Size(256, 256)
			}, {
				isBaseLayer : true,
				singleTile : false
			});
	var baseLayerTrails = new OpenLayers.Layer.WMS(BL_PROV_TRAILS, "http://sdi.provinz.bz.it/geoserver/gwc/service/wms/",
			{
				layers : "WMTS-GMC_BASEMAP-APB-PAB",
				format : "image/png8",
				projection : mapOptions.projection,
				tileOrigin : new OpenLayers.LonLat(-20037508.34, -20037508.34),
				tileSize : new OpenLayers.Size(256, 256)
			}, {
				isBaseLayer : true,
				singleTile : false
			});
*/
	var querypointLayer = new OpenLayers.Layer.Vector(QUERYPOINT_NAME, {
		displayInLayerSwitcher : false
	});
	querypointLayer.styleMap = new OpenLayers.StyleMap({
		"default" : new OpenLayers.Style({
			externalGraphic : "./img/poi_black.png",
			pointRadius : 10
		}),
		"select" : new OpenLayers.Style({
			pointRadius : 12
		})
	});
	querypointLayer.events
			.on({
				"featureselected" : function(e) {
					var lonLat = new OpenLayers.LonLat(e.feature.geometry.x, e.feature.geometry.y);
					var popup = new OpenLayers.Popup("chicken", lonLat, new OpenLayers.Size(80, 20), IsoUtil.toHtml(tArrival),
							false);
					popup.setBackgroundColor('#CCCCCC');
					popup.setOpacity(0.9);
					popup.setBorder("2px solid");
					e.feature.popup = popup;
					map.addPopup(popup, true);
				},
				"featureunselected" : function(e) {
					if (e.feature.popup) {
						e.feature.popup.destroy();
					}
				}
			});

	var baseLayerOffline = new OpenLayers.Layer.WMS(BL_OFFLINE, config.mapserverUrl + "/wms", {
		layers : "pbz_base",
		format : "image/png8",
		projection : mapOptions.projection
	}, {
		isBaseLayer : true,
		singleTile : false,
		visibility : false,
		displayOutsideMaxExtent : true,
	});
  globalLayers[BL_OFFLINE] = baseLayerOffline;
	
	//map.addLayers([ baseLayerGoogle, baseLayerOpenStreet, querypointLayer]);
	map.addLayers([ baseLayerGoogle, baseLayerOpenStreet, querypointLayer,baseLayerOffline]);

	map.setLayerIndex(querypointLayer, 5);
	globalLayers[BL_GOOGLE] = baseLayerGoogle;
	globalLayers[BL_OSM] = baseLayerOpenStreet;
	//globalLayers[BL_PROV_OF_2011] = baseLayerOrthofoto2011;
	//globalLayers[BL_PROV_TRAILS] = baseLayerTrails;
	globalLayers[QUERYPOINT_NAME] = querypointLayer;
	

	var isoForm = OpenLayers.Util.getElement("isoForm");
	var coord = isoForm["queryPointField"].value.split(' ');
	var qPointFeature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(coord[0], coord[1]), {
		color : "#000000"
	});
	var features = new Array();
	features.push(qPointFeature);
	querypointLayer.addFeatures(features);
	querypointLayer.drawFeature(qPointFeature);

	var ctrlDrawFeature = new OpenLayers.Control.DrawFeature(querypointLayer, OpenLayers.Handler.Point);
	ctrlDrawFeature.featureAdded = function(feature) {
		if (!multipleQueryPointSelection) {
			updatePoiField(feature.geometry);
			var featuresToDelete = new Array();
			var features = querypointLayer.features;
			for ( var i = 0; i < querypointLayer.features.length; i++) {
				var currentFeature = querypointLayer.features[i];
				if (currentFeature.id != feature.id) {
					featuresToDelete.push(currentFeature);
				}
			}
			if (featuresToDelete.length > 0) {
				querypointLayer.removeFeatures(featuresToDelete);
			}
			delete featuresToDelete;
		}
	};
	globalControls[CTRL_DRAW_FEATURE] = ctrlDrawFeature;
	map.addControl(ctrlDrawFeature);
	// new
	var ctrlDragQPoint = new OpenLayers.Control.DragFeature(querypointLayer);
	ctrlDragQPoint.onComplete = function(feature) {
		updatePoiField(feature.geometry);
		submitIsoForm();
	}
	globalControls[CTRL_DRAG_QPOINT_FEATURE] = ctrlDragQPoint;
	map.addControl(ctrlDragQPoint);
	ctrlDragQPoint.activate();

	addSingleQueryPointSelector(querypointLayer);
	map.addControl(new OpenLayers.Control.LayerSwitcher());
	map.addControl(new OpenLayers.Control.MousePosition());
	map.addControl(new OpenLayers.Control.Navigation());
	map.addControl(new OpenLayers.Control.PanZoom());
	map.zoomToExtent(mapOptions.maxExtent, true);
}

function addSingleQueryPointSelector(qPointlayer) {
	var ctrlSelectFeature = new OpenLayers.Control.SelectFeature(qPointlayer, {
		clickout : false,
		toggle : false,
		multiple : false,
		hover : true
	});
	// delete globalControls.get(CTRL_SELECT_FEATURE);
	globalControls[CTRL_SELECT_FEATURE] = ctrlSelectFeature;
	map.addControl(ctrlSelectFeature);
	ctrlSelectFeature.activate(); // can only be activated after it is added
	// to the map
}

/**
 * Updates the poi field with the specified values, that will be rounded
 * 
 * @param {Object}
 *          OpenLayers.Geometry.Point
 */
function updatePoiField(point) {
	var poiElement = OpenLayers.Util.getElement("queryPointField");
	poiElement.value = Math.round(point.x) + ", " + Math.round(point.y);
}

/**
 * Updates the arrival time field with the specified converted value
 * 
 * @param {String}
 *          the datetime string with pattern MM/DD/YYYY TT:MM
 */
function updateTArrivalField(datetimeString) {
	// $("#referenceTime").datetimepicker("setDate" , datetimeString);
	tArrival = IsoUtil.string2Date(datetimeString);
	$("#referenceTime").val(datetimeString);
}

/**
 * 
 */
function poiSelection() {
	var poiCtrl = globalControls[CTRL_DRAW_FEATURE];
	if ($("input[name=queryPointSelection]").attr('checked')) {
		map.div.style.cursor = 'crosshair';
		poiCtrl.activate();
	} else {
		map.div.style.cursor = null;
		poiCtrl.deactivate();
	}
}

function disableDrawQueryPoint() {
	var poiCtrl = globalControls[CTRL_DRAW_FEATURE];
	if ($("input[name=queryPointSelection]").attr('checked')) {
		$('input[name=queryPointSelection]').attr('checked', false);
		map.div.style.cursor = null;
		poiCtrl.deactivate();
	}
}

/**
 * Should be invoked, when a different dataset is selected from the user
 * interface
 * 
 * @param {Object}
 *          val
 */
function updateDataset(val) {
	$("input[name=enableCoverage]").attr('checked', false);
	$("#queryBufferDistance").attr('disabled', true);

	destroyDynamicLayers();
	var newCfg = datasetCfgs[val];

	var queryPointFeature = new OpenLayers.Feature.Vector(newCfg.queryPoint, {
		calculateInRange : function() {
			return true
		}
	});
	var querypointLayer = globalLayers[QUERYPOINT_NAME];
	querypointLayer.destroyFeatures();
	var features = new Array();
	features.push(queryPointFeature);
	querypointLayer.addFeatures(features);
	map.setCenter(newCfg.queryPoint);
	map.zoomToExtent(newCfg.bbox, true);
	querypointLayer.drawFeature(queryPointFeature);
	updatePoiField(newCfg.queryPoint);
	updateTArrivalField(newCfg.arrivalTime);
	addSingleQueryPointSelector(querypointLayer);
}

function destroyDynamicLayers() {
	var ctrlSelectFeature = globalControls[CTRL_SELECT_FEATURE];
	ctrlSelectFeature.deactivate();
	$("#resultTabPane").css("visibility", "collapse");
	map.removeControl(ctrlSelectFeature);
	var removingLayers = new Array();
	jQuery.each(map.layers, function() {
		var l = globalLayers[this.name];
		if (!l) {
			removingLayers.push(this);
		}
	});
	for ( var i = 0; i < removingLayers.length; i++) {
		map.removeLayer(removingLayers[i]);
		removingLayers[i].destroy();
	}
}

/**
 * displays the progress bar in the center of the map
 */
function showProgressBar() {
	$("#progressBar").progressbar("value", 0);
	var width, height;
	if (map) {
		height = map.getSize().h;
		width = map.getSize().w;
	} else {
		height = document.body.clientHeight;
		width = document.body.clientWidth;
	}
	$("#progressBar").css("top", (height / 2 - (height * 0.05)) + 'px');
	$("#progressBar").css("left", (width / 2 - (width * 0.05)) + 'px');
	$("#progressBar").css("visibility", "visible");
}

function hideProgressBar() {
	$("#progressBar").css("visibility", "collapse");
}

function hideIsoTabPane() {
	$("#isoTabPane").slideUp("fast");
}

/**
 * hides all other open panes and shows the selected pane
 * 
 * @param {Object}
 *          currentTabPane the pane to be shown
 */
function showTabMenu(currentTabPane) {
	for ( var pane in tabPaneStatus) {
		if (pane != currentTabPane) {
			if (tabPaneStatus[pane] == true) {
				tabPaneStatus[pane] = false;
				$(pane).slideUp("fast");
			}
		}
	}
	tabPaneStatus[currentTabPane] = true;
	$(currentTabPane).slideToggle("slow");
	return false;
}

function submitIsoForm() {
	var LOG_PARSING_TIME = 0;
	var LOG_RENDERING_TIME = 0;
	var startTime = Date.now();
	hideIsoTabPane();
	showProgressBar();
	disableDrawQueryPoint();
	destroyDynamicLayers();

	var queryParameters = new Object();
	var webServiceForm = OpenLayers.Util.getElement("isoForm");
	for ( var i = 0; i < webServiceForm.elements.length; ++i) {
		var element = webServiceForm.elements[i];
		var val = '';
		switch (element.type) {
		case "select-one":
			for ( var j = 0; j < element.options.length; j++) {
				if (element.options[j].selected) {
					if (j > 0) {
						val += "," + element.options[j].value;
					} else {
						val += element.options[j].value;
					}
				}
			}
			break;
		case "options":
			for ( var j = 0; j < element.options.length; j++) {
				if (element.options[j].selected) {
					if (j > 0) {
						val += "," + element.options[j].value;
					} else {
						val += element.options[j].value;
					}
				}
			}
			break;
		case "checkbox":
			val = element.checked;
			break;
		case "radio":
			if (element.checked) {
				val = element.value;
			} else {
				continue;
			}
			break;
		case "text":
			val = element.value;
			break;
		}

		if (val && val != "") {
			queryParameters[element.name] = val;
		}
	}

	queryParameters["speed"] = IsoUtil.kmp2mps(queryParameters["speed"]);
	queryParameters["dMax"] = IsoUtil.minutes2seconds(queryParameters["dMax"]);

	var enableCoverage = $("input[name=enableCoverage]").attr('checked');
	var expirationMode = queryParameters["showExpiration"];

	if (!enableCoverage) {
		delete queryParameters.bufferDistance;
	} else {
		queryParameters["bufferDistance"] = parseFloat(queryParameters["bufferDistance"]);
	}

	// adding all the querypoints from the querypoint layer
	var queryPointFeatures = globalLayers[QUERYPOINT_NAME].features;
	var queryPoints = "[";
	for ( var i = 0; i < queryPointFeatures.length; i++) {
		var point = queryPointFeatures[i];
		queryPoints += "[" + point.geometry.x + "," + point.geometry.y + "]";
		if (i < queryPointFeatures.length - 1) {
			queryPoints += ",";
		}
	}
	queryPoints += "]";
	queryParameters["queryPoints"] = queryPoints;

	var layers = new Array();

	var nodeStyleMap = new OpenLayers.StyleMap({
		"default" : new OpenLayers.Style({
			pointRadius : 8
		}),
		"select" : new OpenLayers.Style({
			pointRadius : 10
		})
	});

	if (expirationMode) {
		var nodeLookup = {
			"CLOSED" : {
				'strokeColor' : '#000000',
				'fillColor' : '#000000'
			},
			"OPEN" : {
				'strokeColor' : '#000000',
				'fillColor' : '#ffffff'
			},
			"EXPIRED" : {
				'strokeColor' : '#000000',
				'fillColor' : '#c2c2c2'
			}
		}
		nodeStyleMap.addUniqueValueRules("default", "status", nodeLookup);
		nodeStyleMap.addUniqueValueRules("select", "status", nodeLookup);

		/*
		 * var nodeLayer = new OpenLayers.Layer.Vector("Iso Nodes", { layers:
		 * "Nodes", calculateInRange: function(){ return true; }, renderers: ["SVG",
		 * "Canvas", "VML"], styleMap: nodeStyleMap }); layers.push(nodeLayer);
		 */

	} else {
		if (queryParameters["mode"] != "UNIMODAL") {
			var lookup = {
				0 : {
					externalGraphic : imgLookup[0]
				},
				1 : {
					externalGraphic : imgLookup[1]
				},
				2 : {
					externalGraphic : imgLookup[2]
				},
				3 : {
					externalGraphic : imgLookup[3]
				},
				4 : {
					externalGraphic : imgLookup[4]
				},
				5 : {
					externalGraphic : imgLookup[5]
				},
				6 : {
					externalGraphic : imgLookup[6]
				},
				7 : {
					externalGraphic : imgLookup[7]
				}
			};

			nodeStyleMap.addUniqueValueRules("default", "reachedByType", lookup);
			nodeStyleMap.addUniqueValueRules("select", "reachedByType", lookup);

			/*
			 * var nodeLayer = new OpenLayers.Layer.Vector("Iso Nodes", { layers:
			 * "Nodes", calculateInRange: function(){ return true; }, renderers:
			 * ["SVG", "Canvas", "VML"], styleMap: nodeStyleMap });
			 * 
			 * 
			 * nodeLayer.events.on({ "featureselected": function(e){ var htmlString =
			 * IsoUtil.toHtml(tArrival, e.feature); var popup = new
			 * OpenLayers.Popup("chicken",
			 * e.feature.geometry.getBounds().getCenterLonLat(), new
			 * OpenLayers.Size(170, 40), htmlString, false);
			 * popup.setBackgroundColor('#CCCCCC'); popup.setOpacity(0.9);
			 * popup.setBorder("2px solid"); //popup.updateSize(); e.feature.popup =
			 * popup; map.addPopup(popup, true); }, "featureunselected": function(e){
			 * var popup = e.feature.popup; if (popup) popup.destroy(); } });
			 * layers.push(nodeLayer);
			 */
		}
	}

	var dMax = queryParameters["dMax"];

	var currentDSetConfig = datasetCfgs[queryParameters["dataset"]];

	if (layers.length > 0) {
		map.addLayers(layers);
		map.setLayerIndex(layers[0], 4);
	}

	// layers.push(globalLayers[QUERYPOINT_NAME]);
	var ctrlLayers = new Array();
	var isMultimodal = queryParameters["mode"] != "UNIMODAL" ? true : false;
	var incommingMode= queryParameters["direction"] == "INCOMMING" ? true : false;

	ctrlLayers.push(globalLayers[QUERYPOINT_NAME]);

	var ctrlSelectFeature = new OpenLayers.Control.SelectFeature(ctrlLayers, {
		clickout : false,
		toggle : false,
		multiple : false,
		hover : true
	});

	map.addControl(ctrlSelectFeature);
	ctrlSelectFeature.activate();
	globalControls[CTRL_SELECT_FEATURE] = ctrlSelectFeature;

	var edgeLayer = currentDSetConfig["isoEdgeLayer"];
	var wmsLinkLayer = new OpenLayers.Layer.WMS(edgeLayer["name"], config.mapserverUrl + "/wms", {
		format : "image/png",
		transparent : "TRUE",
		layers : edgeLayer["layer"]
	}, {
		visibility : false,
		maxExtent : map.baseLayer.maxExtent,
		maxResolution : map.baseLayer.maxResolution, // "auto" if not
		// defined
		// in the context
		minResolution : map.baseLayer.minResolution, // "auto" if not
		// defined
		// in the context
		isBaseLayer : false,
		displayOutsideMaxExtent : true,
		transitionEffect : "resize",
		calculateInRange : function() {
			return true
		}
	});

	var wmsNodeLayer;
	if (expirationMode || isMultimodal) {
		var vertexLayer = currentDSetConfig["isoVertexLayer"];
		wmsNodeLayer = new OpenLayers.Layer.WMS(vertexLayer["name"], config.mapserverUrl + "/ows", {
			format : "image/png",
			transparent : "TRUE",
			layers : vertexLayer["layer"],
			styles : expirationMode ? "StyleVertexExpiration" : "StyleTransportationStations"
		}, {
			visibility : true,
			maxExtent : map.baseLayer.maxExtent,
			maxResolution : map.baseLayer.maxResolution, // "auto" if not
			// defined
			// in the context
			minResolution : map.baseLayer.minResolution, // "auto" if not
			// defined
			// in the context
			isBaseLayer : false,
			displayOutsideMaxExtent : true,
			transitionEffect : "resize",
			calculateInRange : function() {
				return true
			}
		});
	}

	/*
	 * 
	 * sm.styles['default'].addRules([ new OpenLayers.Rule({ minScaleDenominator:
	 * 100000, filter: new OpenLayers.Filter.Comparison({ type:
	 * OpenLayers.Filter.Comparison.GREATER_THAN, property: "route_id", value: -1
	 * }), symbolizer: {strokeColor: "cyan"} }), new OpenLayers.Rule({ elseFilter:
	 * true }) ]);
	 * 
	 * 
	 * new OpenLayers.Rule({ minScaleDenominator: 100000, symbolizer:
	 * {graphicWidth: 24, graphicHeight: 27.75, graphicYOffset: -27.75} });
	 * 
	 */

	$.cometd.subscribe('/service/iso', function(message) {

		var start = Date.now()
		var result = jQuery.parseJSON(message.data);
		LOG_PARSING_TIME += Date.now() - start;

		if (result) {
			if (result.currentDistance) {
				var status = Math.round((100 / (dMax/60)) * result.currentDistance);
				OpenLayers.Console.log("Status:" + status);
				$("#progressBar").progressbar("value", status);
			}
			// OpenLayers.Console.log("Junk:" +
			// numberOfReceivedJunks++);
			if (result.features) {
				var features = result.features;
				var nodeFeatures = new Array();
				for ( var i = 0; i < features.length; i++) {
					var feature = features[i];
					if (feature.geometry.type == "Point") {
						// currentDistance =
						// Math.max(currentDistance,feature.properties.distance);
						var coords = feature.geometry.coordinates;
						nodeFeatures.push(new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Point(coords[0], coords[1]),
								feature.properties, null));
					} else if (feature.geometry.type == "LineString") {
						var points = new Array();
						for ( var j = 0; j < feature.geometry.coordinates.length; j++) {
							var coord = feature.geometry.coordinates[j];
							points.push(new OpenLayers.Geometry.Point(coord[0], coord[1]));
						}
						linkFeatures.push(new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(points)));
					}
				}
				if (nodeFeatures.length > 0) {
					// nodeLayer.addFeatures(nodeFeatures);
				}
			}

			if (result.bbox) {
				var bbox = result.bbox;
				hideProgressBar();
				var st1 = Date.now()
				map.addLayer(wmsLinkLayer);
				wmsLinkLayer.setVisibility(true);
				wmsLinkLayer.redraw(true);
				if (wmsNodeLayer) {
					var protocol = OpenLayers.Protocol.WFS.fromWMSLayer(wmsNodeLayer, {
						srsName : CLIENT_EPSG,
						featurePrefix : "iso",
						featureNS : "http://isochrones.inf.unibz.it",
						geometryName : "GEOMETRY"
					});

					var select = new OpenLayers.Layer.Vector("Selection", {
						styleMap : new OpenLayers.Style(OpenLayers.Feature.Vector.style["select"])
					});
					map.addLayers([ wmsNodeLayer, select ]);

					var control = new OpenLayers.Control.GetFeature({
						protocol : protocol,
						hover : false,
						multipleKey : "shiftKey",
						toggleKey : "ctrlKey",
						maxFeatures : 1,
						clickTolerance : 10
					});

					/** 
					 * When feature is selected a popup dialog appears
					 */
					control.events.register("featureselected", this, function(e) {
						var data = e.feature.data;
						//var id = IsoUtil.asId(e.feature.fid);
						var id = data.ID;
						var lonLat = new OpenLayers.LonLat(e.feature.geometry.x, e.feature.geometry.y);
						var distance = data.DISTANCE;
						var distanceInTime = incommingMode ? IsoUtil.subtractSeconds(tArrival, distance) : IsoUtil.addSeconds(tArrival, distance);
						popupFeature = new OpenLayers.Popup.FramedCloud("chicken", lonLat, new OpenLayers.Size(250, 200), IsoUtil
								.asHTML(id, distance, distanceInTime), null, true, closeBoxCallback);
						map.addPopup(popupFeature);
					});

					control.events.register("featureunselected", this, function(e) {
						select.removeFeatures([ e.feature ]);
					});

					control.events.register("hoverfeature", this, function(e) {
						hover.addFeatures([ e.feature ]);
					});
					control.events.register("outfeature", this, function(e) {
						hover.removeFeatures([ e.feature ]);
					});

					map.addControl(control);
					control.activate();

					/*
					 * 
					 * var info = new OpenLayers.Control.WMSGetFeatureInfo({ url:
					 * protocol.url, title: 'Identify features by clicking', layers:
					 * [wmsNodeLayer], output: "object", infoFormat:
					 * "application/vnd.ogc.gml", queryVisible: true, eventListeners: {
					 * getfeatureinfo: function(event) { map.addPopup(new
					 * OpenLayers.Popup.FramedCloud( "chicken",
					 * map.getLonLatFromPixel(event.xy), null, event.text, null, true )); } ,
					 * nogetfeatureinfo : function(event) { ; } } });
					 */

					// map.addLayer(wmsNodeLayer);
					// map.addControl(info);
					// info.activate();
					wmsNodeLayer.setVisibility(true);
					wmsNodeLayer.redraw(true);
				}

				LOG_RENDERING_TIME += Date.now() - st1;

				bbox = new OpenLayers.Bounds(bbox[0], bbox[1], bbox[2], bbox[3]);
				map.zoomToExtent(bbox, false);
				if ($("input[name=enableCoverage]").attr('checked')) {
					getInhabitants(queryParameters["areaComputationMode"], currentDSetConfig);
				}
			}

			if (result.logging) {
				OpenLayers.Console.log("Total response time on client:" + (Date.now() - startTime));
				OpenLayers.Console.log("Parsing time on client:" + LOG_PARSING_TIME);
				OpenLayers.Console.log("WMS time:" + LOG_RENDERING_TIME);
				var logEntries = result.logging;
				$.each(result.logging, function(key, value) {
					OpenLayers.Console.log(key + ": " + value);
				});
			}
		}
		LOG_PARSING_TIME += Date.now() - start;
	});
	$.cometd.publish('/service/iso', queryParameters);

}

function closeBoxCallback(evt) {
	// 'this' is the popup.
	map.removePopup(this);
	map.removePopup(popupFeature);
	this.destroy;
	popupFeature.destroy;
}

function getInhabitants(areaType, currentDSetConfig) {
	// var filter = "CLIENT_ID LIKE\'" + $.cometd.getClientId() + "\'";
	var bufferLayer = currentDSetConfig["isoAreaBufferLayer"];

	/*
	 * var layers = map.getLayersByName(bufferLayer["name"]); if (layers &&
	 * layers.length > 0) { var oldLayer = layers[0]; map.removeLayer(oldLayer);
	 * oldLayer.destroy(); }
	 */

	var isoBufferLayer = new OpenLayers.Layer.WMS(bufferLayer["name"], config.mapserverUrl + "/wms", {
		format : "image/png",
		transparent : "TRUE",
		layers : bufferLayer["layer"]
	}, {
		visibility : false,
		maxExtent : map.baseLayer.maxExtent,
		maxResolution : map.baseLayer.maxResolution, // "auto" if not
		// defined
		// in the context
		minResolution : map.baseLayer.minResolution, // "auto" if not
		// defined
		// in the context
		isBaseLayer : false,
		displayOutsideMaxExtent : true,
		transitionEffect : true,
		calculateInRange : function() {
			return true
		}
	});

	map.addLayer(isoBufferLayer);
	map.setLayerIndex(isoBufferLayer, 2);

	$.cometd.subscribe('/service/inhabitants', function(message) {
		var result = jQuery.parseJSON(message.data);
		if (result) {
			fillResultTabPane(result);
			isoBufferLayer.redraw(true);
			isoBufferLayer.setVisibility(true);
		}
	});
	$.cometd.publish('/service/inhabitants', null);
}

function initEmail() {
	setEmail("emailInnerebner", "markus.innerebner", "inf.unibz.it", "email");
	setEmail("emailGamper", "johann.gamper", "inf.unibz.it", "email");
}

function changeMenuItem(rootMenu) {
	var rootElement = document.getElementById(rootMenu);
}

function setEmail(id, name, domainName, appearName) {
	var el = document.getElementById(id);
	if (el) {
		var anchorEl = document.createElement("a");
		anchorEl.setAttribute("href", "mailto:" + name + "@" + domainName);
		anchorEl.appendChild(document.createTextNode(appearName));
		el.appendChild(anchorEl);
	}
}

function fillResultTabPane(obj) {
	$("#resultContainer").empty();
	var infoString = "<b>Statistics:</b><br/>"
	infoString += "<span>Reached inhabitants: " + obj.reachedInhabitants + "</span>";
	infoString += " <br/> ";
	infoString += "<span>Total inhabitants: " + obj.totalInhabitants + "</span>";
	infoString += " <br/> ";
	infoString += "<span>Percentage: " + obj.averageInhabitants + "%</span>";
	$("#resultContainer").append(infoString);
	$("#resultTabPane").css("visibility", "visible");
	OpenLayers.Console.log("Time iso area initialization: " + obj.isoAreaInitTime);
	OpenLayers.Console.log("Time iso area calculation: " + obj.isoAreaCalculationTime);
	OpenLayers.Console.log("Time stats computation: " + obj.statsComputationTime);

}

function loadSchedules(stopId) {

	var params = new Object();
	params["stopId"] = stopId;

	$.cometd.subscribe('/service/showRouteDetails', function(message) {
		var result = jQuery.parseJSON(message.data);
		var routeTableElement = document.createElement('table');
		routeTableElement.setAttribute("class", "featureInfo");
		var headerRow = document.createElement("tr");
		var headerCell1 = document.createElement("th");
		headerCell1.appendChild(document.createTextNode("Route"));
		var headerCell2 = document.createElement("th");
		headerCell2.appendChild(document.createTextNode("Arrivaltime"));
		var headerCell3 = document.createElement("th");
		headerCell3.appendChild(document.createTextNode("Departuretime"));

		headerRow.appendChild(headerCell1);
		headerRow.appendChild(headerCell2);
		headerRow.appendChild(headerCell3);
		routeTableElement.appendChild(headerRow);

		jQuery.each(result.routeDetails, function() {
			var routeType = this.routeType;
			var routeName = this.routeName;
			var arrivalTime = this.arrivalTime;
			var departureTime = this.departureTime;

			var row = document.createElement('tr');
			var cell1 = document.createElement('td');
			var cell2 = document.createElement('td');
			var cell3 = document.createElement('td');

			var imgEl = document.createElement('img');
			imgEl.setAttribute("src", imgLookup[routeType]);
			imgEl.style.width = '15px';
			imgEl.style.height = '15px';

			cell1.appendChild(imgEl);
			cell1.appendChild(document.createTextNode(routeName));
			cell2.appendChild(document.createTextNode(arrivalTime));
			cell3.appendChild(document.createTextNode(departureTime));
			row.appendChild(cell1);
			row.appendChild(cell2);
			row.appendChild(cell3);
			routeTableElement.appendChild(row);

		});

		var showScheduleElement = document.getElementById('loadSchedulesLink');
		var cellElement = document.getElementById('loadSchedulesCell');
		cellElement.replaceChild(routeTableElement, showScheduleElement);

		// document.getElementById("chicken").style.width="300px";
		// document.getElementById("chicken_contentDiv").style.width="300px";
		// $('chicken').width('300px');
		// $("chicken_contentDiv").width("300px");
		var text = $('chicken').html();

		popupFeature.updateSize();
		// alert(text);
	});
	$.cometd.publish('/service/showRouteDetails', params);

}
