<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
 <head>
  <meta name="generator" content="HTML Tidy, see www.w3.org">
  <!--###### included javascript libraries ######--><!--###### jQuery ######--><script type="text/javascript" src="./lib/jquery/jquery-1.4.2.min.js"></script>
  <script type="text/javascript" src="./lib/jquery/jquery-ui-1.8.6.custom.min.js"></script>
  <script type="text/javascript" src="./lib/jquery/jquery.json-2.2.js"></script>
  <script type="text/javascript" src="./lib/jquery/jquery-ui-timepicker-addon.js"></script>
  <link rel="stylesheet" type="text/css" href="./lib/jquery/css/ui-lightness/jquery-ui-1.8.6.custom.css" />
  <!--###### cometd ######--><script type="text/javascript" src="./org/cometd.js"></script>
  <script type="text/javascript" src="./lib/jquery/jquery.cometd.js"></script>
  <!--###### Google ######-->
  <script src="http://maps.google.com/maps/api/js?v=3.6&amp;sensor=false"></script>
  <!--###### Firebug console ######--><script src="./lib/firebug/firebug.js" type="text/javascript"></script>
  <!--###### Openlayers ######--><script src="./lib/openlayers/OpenLayers.js" type="text/javascript"></script>
  <link rel="stylesheet" href="./lib/openlayers/theme/default/style.css" type="text/css"/>
  <!--###### Isochrones ######--><script src="./lib/isochrones/main.js" type="text/javascript"></script>
  <link rel="stylesheet" type="text/css" href="./css/styles.css"/>
  <script type="text/javascript">
   var config = {
     contextPath: "${pageContext.request.contextPath}"
   };
  </script>
  <title>Computing Isochrones in Multimodal Spatial Networks</title>
 </head>
 <body>
  <div id="map" style="position: relative;">
  </div>
  <div class="styleIsoTabMenu">
   <div id="isoTabMenu">
    <a href="#" class="styleTapPaneLink"><span class="menuItem">ISOCHRONE</span><span style="text-align: right;"><img alt="" id="isoTabImg" class="styleArrows" border="0" src="./img/arrowDown.png" title="Show"></span></a>
   </div>
   <div id="isoTabPane" class="styleIsoTabPane" style="display: none; z-index: 999">
    <form id="isoForm" name="isochroneForm">
     <table width="100%">
      <tr>
       <td>
        <div class="styleParameters">
       		<fieldset>
  					<legend>Location</legend>
		         <input id="BZ" name="dataset" title="Bozen/Bolzano" value="BZ" type="radio" checked="checked" onclick="updateDataset('BZ');">
						 <label>Bozen/Bolzano</label>
						 <input id="ST" name="dataset" title="South Tyrol" value="ST" type="radio" onclick="updateDataset('ST');">
					 	 <label>South Tyrol</label><br/>
		         <input id="SF" name="dataset" title="San Francisco" value="SF" type="radio" onclick="updateDataset('SF');"> 
		         <label>San Francisco</label>
		         <input id="WDC" name="dataset" title="Washington DC" value="WDC" type="radio" onclick="updateDataset('WDC');">
						 <label>Washington DC</label><br/>
						 <input id="IT" name="dataset" title="Italy" value="IT" type="radio" onclick="updateDataset('IT');">
						 <label>Italy</label>
					</fieldset>
				 <fieldset>
  					<legend>Input parameters</legend>
						<table>
							<tr>
								<td><label>Time:</label></td>
								<td><input type="text" id="referenceTime" size="15" value="11/06/2010 10:00" name=referenceTime></td>
							</tr>	
							<tr>
								<td><label>Max. time span:</label></td>
								<td><input type="text" id="dMax" name="dMax" style="width: 40px" value="15" title="Specify the maximum duration"><span class="styleLabel">(minutes)</span></td>
							</tr>
							<tr>
								<td><label>Walking speed:</label></td>
								<td><input type="text" name="speed" value="2" style="width: 30px" title="Specify the waliking speed in kmph"><span class="styleLabel">(kmph)</span></td>
							</tr>
							<tr>
								<td><label>Query point:</label></td>
								<td><input id="queryPointField" type="text" name="queryPoints" value="1263555, 5860638" class="inputMandatory" size="15" title="X,Y coordinates in Google Projection format"></td>
							</tr>
							<tr>
								<td colspan="2">
									<label>Select query point:</label>
									<input id="queryPointSelectionId" type="checkbox" name="queryPointSelection" title="By activating the coordinates of the query point can be selected per mouse click" onclick="poiSelection();">
									&nbsp;&nbsp;
         					<label>Multiple query points:</label>
									<input id="multipleQueryPointId" type="checkbox" name="multipleSelection" title="By activating multiple query points can be choosen" onclick="changeQPointSelectionMode();">
								</td>
							</tr>
						</table>
						<fieldset id="transportationMode">
         			<legend>Mode</legend>
							<input name="mode" title="You reach the query point only by walking" value="UNIMODAL" type="radio">
		          <label>Unimodal<img alt="" src="./img/ped.png" class="nwIcon"></label>
							&nbsp;&nbsp;
							<input name="mode" title="You reach the query point by a combination of walking and using the means of transport" value="MULTIMODAL" type="radio" checked="checked">
							<label>Multimodal<img alt="" src="./img/ped.png" class="nwIcon">+ <img alt="" src="./img/bus.png" class="nwIcon"></label>
						</fieldset>
						<fieldset id="directionMode">
         			<legend>Direction</legend>
							<input name="direction" title="Considers the incoming directions" value="INCOMMING" type="radio" checked="checked">
		          <label>Incoming</label>
							&nbsp;&nbsp;
							<input name="direction" title="Considers the outgoing directions" value="OUTGOING" type="radio">
							<label>Outgoing</label>
						</fieldset>  
				  </fieldset>
				<fieldset>
  				<legend>Reachability statistics</legend>
					<label>Compute: </label> <input id="enableCoverage" type="checkbox" name="enableCoverage" title="Specify if you want to obtain statistics about the reachabilty" onclick="enableCoverageMode();">
	         <label>Buffer distance:</label><input id="queryBufferDistance" type="text" name="bufferDistance" value="20" class="inputMandatory" size="3" title="The buffer distance" disabled="disabled">
					 <br/>
	         <input name="areaComputationMode" title="Link-based approach" value="LBA" type="radio" >
			     <label>Link-Based-Area</label>
					 <input name="areaComputationMode" title="Surface-based approach" value="SBA" type="radio" checked="checked">
					 <label>Surface-Based-Area</label>
        </fieldset>
				<fieldset>
  				<legend>Presentation mode</legend>
         	<label>Show expiration:  <input  type="checkbox" name="showExpiration" title="Enables expiration presententation"></label>
        </fieldset>
				  
        <br/>
        <div style="text-align: center; float: none;">
         <input name="Retrieve" onclick="submitIsoForm();" class="btn" type="button" value="Compute Isochrones" title="By clicking this button the isochrone computation will be started.">
        </div>
				</div>
       </td>
      </tr>
     </table>
    </form>
   </div>
  </div>
  <div class="styleHelpTabMenu">
   <div id="helpTabMenu">
    <a href="#" class="styleTapPaneLink"><span class="menuItem">HELP</span><span><img alt="" id="helpTabImg" class="styleArrows" border="0" src="./img/arrowDown.png" title="Show"></span></a>
   </div>
   <div id="helpTabPane" class="styleHelpTabPane" style="display: none; z-index: 999">
    <div class="menuText">
     <h3>Instructions:</h3>
     <p>
      To start - click on the "ISOCHRONE" in the top-right corner of
      the screen. The options dialog will appear.
     </p>
     <p>
      Select the options for the computation of the isochrone:
     </p>
     <ul>
      <li>
       <b>Location -</b>
       the place where the isochrone should be
       computed.
      </li>
      <li>
       <b>Time -</b>
       the latest possible arrival time at which the
       point of interest can be reached when incomming direction is considered, otherwise the departure time.
      </li>
      <li>
       <b>Duration -</b>
       amount of minutes that represents the time at disposal.
      </li>
      <li>
       <b>Walking speed -</b>
       the walking speed in kilometers per
       hour.
      </li>
      <li>
       <b>Query point -</b>
       the coordinates of the point of
       interest (the black star in the map). You can also drag the query point and after positioning the query is launched. It is possible to select multiple query points.
      </li>
      <li>
       <b>Mode - </b>
       the mode how the network is visited. Possible
       modes are only walking or combing walking and the use of means of
       transport
      </li>
      <li>
       <b>Direction - </b>
       Incoming Considers all the part in the network that reaches the query point, outgoing all part in the network, reached from the query point.
      </li>
      <li>
       <b>Reachbility statistics - </b>
      	Enables to obtain a percentag of inhabitants that reaches the query point or that are reached by the query point.
      </li>
      <li>
       <b>Presentation mode - </b>
      	Illustrates the expiration mode describe in the paper SSDB12, when enabled. 
      </li>
     </ul>
     <br/>
     <br/>
     <br/>
     <br/>
     <p>
      Click the button "Compute Isochrones" to start the isochrone
      computation
     </p>
     <p>
      After a short time the result (green labeled lines and reached transportation stations) are displayed
      on the map
     </p>
    </div>
   </div>
  </div>
  <div class="styleHomeTabMenu">
   <div id="homeTabMenu">
    <a href="http://www.isochrones.inf.unibz.it" class="styleTapPaneLink"><span class="menuItem">HOME</span></a>
   </div>
  </div>
  <div id="resultTabPane" class="styleResultTabPane" style="visibility: collapse; z-index: 999">
   <div id="resultContainer">
    No results
   </div>
  </div>
  <div id="progressBar" class="styleProgressBar">
  </div>
 </body>
</html>
