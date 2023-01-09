package com.wassimbh.cogistest.ui.map

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.geojson.Polygon
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.Annotation
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.wassimbh.cogistest.R
import com.wassimbh.cogistest.data.models.GraphEdge
import com.wassimbh.cogistest.data.models.MyLocation
import com.wassimbh.cogistest.data.models.PathNode
import com.wassimbh.cogistest.data.models.PoiEntity
import com.wassimbh.cogistest.databinding.FragmentMapBinding
import com.wassimbh.cogistest.ui.adapters.ListAdapter
import com.wassimbh.cogistest.ui.base.BaseFragment
import com.wassimbh.cogistest.ui.main.MainViewModel
import com.wassimbh.cogistest.utilities.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(), OnClickListener,
    OnPointAnnotationDragListener, OnRecyclerItemClick<PoiEntity>, OnMapClickListener {
    override val layoutResourceId = R.layout.fragment_map

    private val mViewModel: MapViewModel by viewModels()
    private val mSharedViewModel: MainViewModel by activityViewModels()

    private lateinit var polygonAnnotationManager: PolygonAnnotationManager
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var polyLineAnnotationManager: PolylineAnnotationManager

    private lateinit var selectedLocation: MyLocation
    private val initialPoint = fromLngLat(2.5008220528314666, 48.7519833027344)
    private var itineraryStartingPoint: Point? = null
    private var selectedPoi: PoiEntity? = null
    private var itineraryPolyline: MutableList<PolylineAnnotation> = mutableListOf()
    private var itineraryPoint: MutableList<PointAnnotation> = mutableListOf()

    private val initialCameraPosition = cameraOptions {
        center(initialPoint) // Sets the new camera position on click point
        zoom(18.5) // Sets the zoom
        bearing(0.0) // Rotate the camera
        pitch(0.0) // Set the camera pitch
        mapAnimationOptions {
            duration(3000)
        }
    }

    private var inItineraryMode = false

    override fun setUpView() {
        super.setUpView()

        mDataBinding.tvOffice.setOnClickListener(this)
        mDataBinding.tvSousSol.setOnClickListener(this)
        mDataBinding.tvOfficeVisibility.setOnClickListener(this)
        mDataBinding.tvSousSolVisibility.setOnClickListener(this)
        mDataBinding.tvOfficeMarker.setOnClickListener(this)
        mDataBinding.tvRdcMarker.setOnClickListener(this)
        mDataBinding.tvAdd.setOnClickListener(this)
        mDataBinding.tvCancel.setOnClickListener(this)
        mDataBinding.rlItinerary.setOnClickListener(this)
        mDataBinding.ivAddItinerary.setOnClickListener(this)
        mDataBinding.ivRemoveItinerary.setOnClickListener(this)

        mDataBinding.loading.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
    }

    override fun viewModelObserver() {
        super.viewModelObserver()
        mViewModel.poiList.observe(viewLifecycleOwner) {
            setupItRv(it)
        }

        mSharedViewModel.itineraryList.observe(viewLifecycleOwner) {
            Coroutines.main {
                Timber.e("MapBox Points: Calling method for size: ${it.size}")
                mDataBinding.loading.isVisible = false
            }
        }

    }

    private fun setupItRv(list: List<PoiEntity>) {
        val mAdapter = ListAdapter(emptyList())
        mAdapter.setupClickListener(this)
        mDataBinding.rvPoi.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = mAdapter
            mAdapter.updateList(list.toMutableList())
        }
    }

    private fun drawItinerary(startingPoint: Point, destination: PoiEntity) {
        mSharedViewModel.edgesLiveData.value?.let { l ->
            var edges = l
            val startingPoi = PoiEntity(
                0,
                startingPoint.longitude(),
                startingPoint.latitude(),
                "Start",
                FloorsEnum.RDC.name

            )
            val filteredEdges =
                edges.filter { it.floorValue == startingPoi.getFloorValue() && !it.floorChange }
            val destinationFloorEdge =
                mSharedViewModel.getClosestChangingFloorEdges(startingPoi, destination, edges)
            val finalEdge = mSharedViewModel.getEdgeFromList(destination.toPoint(), edges, false)


            val beginningEdge = mSharedViewModel.getEdgeFromList(startingPoint, edges, true)
            val differentFloor = startingPoi.getFloorValue() != destination.getFloorValue()
            if (differentFloor) {
                edges = filteredEdges.toMutableList()
                if (finalEdge != null && destinationFloorEdge != null) {
                    val finalEdges = edges.filter { it.floorValue == destination.getFloorValue() }
                        .toMutableList()
                    finalEdges.add(destinationFloorEdge)

                    drawBetweenTwoPoints(
                        PathNode(destinationFloorEdge.destination),
                        PathNode(finalEdge.destination),
                        destinationFloorEdge.point.second,
                        destination.toPoint(),
                        finalEdges
                    )

                }
                if (beginningEdge != null && destinationFloorEdge != null) {

                    val finalEdges = edges.filter { it.floorValue == startingPoi.getFloorValue() }
                        .toMutableList()

                    drawBetweenTwoPoints(
                        PathNode(beginningEdge.start),
                        PathNode(destinationFloorEdge.start),
                        startingPoint,
                        destinationFloorEdge.point.first,
                        finalEdges
                    )
                }
            } else {
                if (beginningEdge != null && finalEdge != null) {

                    val finalEdges = edges.filter { it.floorValue == startingPoi.getFloorValue() }
                        .toMutableList()

                    drawBetweenTwoPoints(
                        PathNode(beginningEdge.start),
                        PathNode(finalEdge.destination),
                        startingPoint,
                        destination.toPoint(),
                        finalEdges
                    )
                }
            }
        }
    }

    private fun drawBetweenTwoPoints(
        startingNode: PathNode,
        endingNode: PathNode,
        startingPoint: Point,
        endingPoint: Point,
        finalEdges: List<GraphEdge>
    ) {
        val graphList = mSharedViewModel.edges.sortedBy { (it.node1 as PathNode).number }
        val result = findShortestPath(graphList, startingNode, endingNode)
        val path = result.shortestPath()
        renderItinerary(startingPoint, endingPoint, path, finalEdges)
    }

    private fun renderItinerary(
        beginningPoint: Point,
        finalPoint: Point,
        path: List<Node>,
        edges: List<GraphEdge>
    ) {
        val itineraryEdges = mutableListOf<GraphEdge>()
        val points = mutableListOf(beginningPoint)
        path.forEach { n ->
            val node = n as PathNode
            edges.find { it.start == node.number || it.destination == node.number }?.let {
                if (it.start != node.number) {
                    it.reversePoints()
                }
                itineraryEdges.add(it)
            }
        }
        itineraryEdges.forEach {

            if (it.floorChange) {
                val icon =
                    if (it.from!! < it.to!!) R.drawable.ic_arrow_upward else R.drawable.ic_arrow_downward
                itineraryPoint.add(addSingleMarker(null, it.toPoi(true), false, icon)!!)
            }
            points.add(it.point.first)
        }
        points.add(finalPoint)

        itineraryPolyline.addAll(polyLineAnnotationManager.create(getPolyLineAnnotation(points)))

        Timber.e("$points")
    }

    private fun redirect() {
        mDataBinding.mapView.getMapboxMap().flyTo(initialCameraPosition)
    }

    private fun getLineStringsFromGeoJson(jsonStr: String): List<LineString> {

        // Get GeoJSON features from GeoJSON file in the assets folder
        val featureCollection =
            FeatureCollection.fromJson(jsonStr)

        val lineStringList = ArrayList<LineString>()

        if (featureCollection.features() != null) {
            for (singleLocation in featureCollection.features()!!) {
                if (singleLocation.geometry() is Polygon) {
                    (singleLocation.geometry() as Polygon).outer()?.let {
                        lineStringList.add(it)
                    }
                }
            }
        }
        return lineStringList
    }

    private fun getPolyLineAnnotationOptions(geoJsonStr: String): List<PolylineAnnotationOptions> {

        val list = ArrayList<PolylineAnnotationOptions>()
        val lineStringsList = getLineStringsFromGeoJson(geoJsonStr)
        lineStringsList.forEach { lineString ->
            val polylineAnnotationOptions: PolylineAnnotationOptions =
                PolylineAnnotationOptions()
                    .withPoints(lineString.coordinates())
                    .withLineColor("#222B51")
                    .withLineWidth(0.5)

            list.add(polylineAnnotationOptions)
        }
        return list
    }

    private fun getPolyLineAnnotation(
        points: List<Point>,
        colorStr: String? = null
    ): List<PolylineAnnotationOptions> {
        val color = colorStr ?: "#229E9E"

        val list = ArrayList<PolylineAnnotationOptions>()
        val polylineAnnotationOptions: PolylineAnnotationOptions =
            PolylineAnnotationOptions()
                .withPoints(points)
                .withLineColor(color)
                .withLineWidth(2.5)
        list.add(polylineAnnotationOptions)

        return list
    }

    private fun setupMap() {
        Coroutines.main {
            mDataBinding.mapView.apply {
                redirect()
                getMapboxMap().addOnMapClickListener(this@MapFragment)
                val annotationConfig = AnnotationConfig()

                polygonAnnotationManager =
                    annotations.createPolygonAnnotationManager(annotationConfig)
                pointAnnotationManager = annotations.createPointAnnotationManager(annotationConfig)
                pointAnnotationManager.addDragListener(this@MapFragment)
                polyLineAnnotationManager =
                    annotations.createPolylineAnnotationManager(annotationConfig)

                getMapboxMap().loadStyleUri(Style.OUTDOORS) {

                    mSharedViewModel.mLocations.forEach {
                        if (it.isDisplayed) {
                            deleteBuildingFromMap(it)
                            renderBuildingInMap(it)
                        }
                    }
                }
            }
        }
    }

    private fun manageBuilding(location: MyLocation) {

        if (location.isDisplayed) {
            deleteBuildingFromMap(location)
        } else {
            renderBuildingInMap(location)
        }
    }

    private fun renderBuildingInMap(location: MyLocation) {
        location.polygonAnnotations =
            polygonAnnotationManager.create(location.annotationOptions)

        if (location.withPolyLine) {
            location.polylineAnnotation =
                polyLineAnnotationManager.create(getPolyLineAnnotationOptions(location.jsonStr))
        }
        renderBuildingMarkers(location)
        location.isDisplayed = true
    }

    private fun renderBuildingMarkers(location: MyLocation) {
        mViewModel.getPoi(location.floorType.name).observe(viewLifecycleOwner) { list ->
            list.forEach {
                addSingleMarker(location, it, draggable = true)
            }
        }
    }

    private fun deleteBuildingFromMap(location: MyLocation) {
        if (location.isDisplayed) {
            location.polygonAnnotations?.let {
                polygonAnnotationManager.delete(it)
            }
            if (location.withPolyLine) {
                location.polylineAnnotation?.let {
                    polyLineAnnotationManager.delete(it)
                }
            }
            if (location.pointAnnotation.isNotEmpty()) {
                pointAnnotationManager.delete(location.pointAnnotation)
            }
            location.isDisplayed = false
        }
    }


    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    override fun onMapClick(point: Point): Boolean {
        Timber.e("$point")
        mDataBinding.elOffice.isExpanded = false
        mDataBinding.elSousSol.isExpanded = false
        if (inItineraryMode) {
            itineraryStartingPoint = point
            itineraryPoint.add(
                addSingleMarker(
                    null, PoiEntity(
                        0,
                        point.longitude(),
                        point.latitude(),
                        "Start",
                        ""
                    ),
                    false
                )!!
            )
            if (selectedPoi == null)
                Toast.makeText(
                    requireContext(),
                    "Choose a location from the point of interest list",
                    Toast.LENGTH_LONG
                ).show()
            else {
                drawItinerary(itineraryStartingPoint!!, selectedPoi!!)
                inItineraryMode = false
            }
        }
        return true
    }

    override fun onClick(v: View) {
        when (v) {
            mDataBinding.tvOffice -> {
                if (mDataBinding.elSousSol.isExpanded) {
                    mDataBinding.elSousSol.isExpanded = false
                }
                mDataBinding.elOffice.toggle()
            }
            mDataBinding.tvSousSol -> {
                if (mDataBinding.elOffice.isExpanded) {
                    mDataBinding.elOffice.isExpanded = false
                }
                mDataBinding.elSousSol.toggle()
            }
            mDataBinding.tvOfficeVisibility -> {
                mSharedViewModel.mLocations.find { it.floorType == FloorsEnum.RDC }?.let {
                    manageBuilding(it)
                }
            }
            mDataBinding.tvSousSolVisibility -> {
                mSharedViewModel.mLocations.find { it.floorType == FloorsEnum.SOUS_SOL }?.let {
                    manageBuilding(it)
                }
            }
            mDataBinding.tvOfficeMarker -> {
                mSharedViewModel.mLocations.find { it.floorType == FloorsEnum.RDC }?.let {
                    handleLocationMarker(it)
                }
            }
            mDataBinding.tvRdcMarker -> {
                mSharedViewModel.mLocations.find { it.floorType == FloorsEnum.SOUS_SOL }?.let {
                    handleLocationMarker(it)
                }
            }
            mDataBinding.tvAdd -> {
                showDialog()
            }
            mDataBinding.tvCancel -> {
                mDataBinding.elItineraryTools.isExpanded = false
                mDataBinding.lnToolsBtn.isVisible = false
                mDataBinding.tvOffice.isVisible = true
                mDataBinding.tvSousSol.isVisible = true
                mDataBinding.elPoiList.isExpanded = false
                inItineraryMode = false
                mViewModel.resetSelection()
            }
            mDataBinding.rlItinerary -> {
                mDataBinding.elItineraryTools.isExpanded = true
                mDataBinding.tvOffice.isVisible = false
                mDataBinding.tvSousSol.isVisible = false
                mDataBinding.elSousSol.isExpanded = false
                mDataBinding.elOffice.isExpanded = false
                mDataBinding.lnToolsBtn.isVisible = true
                mDataBinding.tvAdd.isVisible = false
            }
            mDataBinding.ivAddItinerary -> {
                mDataBinding.elPoiList.isExpanded = true
                inItineraryMode = true
            }
            mDataBinding.ivRemoveItinerary -> {
                inItineraryMode = false
                deleteItinerary()
            }
        }
    }

    private fun deleteItinerary() {
        polyLineAnnotationManager.delete(itineraryPolyline)
        itineraryPoint.forEach {
            pointAnnotationManager.delete(it)
        }
        mDataBinding.elPoiList.isExpanded = false
        itineraryPoint.clear()
        selectedPoi = null
        itineraryStartingPoint = null
        mViewModel.resetSelection()
    }

    private fun handleLocationMarker(location: MyLocation) {
        centerCamera()
        selectedLocation = location
        if (!location.isDisplayed)
            manageBuilding(location)
        mDataBinding.tvOffice.isVisible = false
        mDataBinding.tvSousSol.isVisible = false
        mDataBinding.elSousSol.isExpanded = false
        mDataBinding.elOffice.isExpanded = false
        mDataBinding.lnToolsBtn.isVisible = true
    }

    private fun centerCamera() {
        mDataBinding.mapView.getMapboxMap().flyTo(
            cameraOptions {
                center(initialPoint) // Sets the new camera position on click point
                zoom(19.0) // Sets the zoom
                bearing(-13.0) // Rotate the camera
                pitch(0.0) // Set the camera pitch
                mapAnimationOptions {
                    duration(2000)
                }
            })
    }

    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<TextView>(R.id.tv_confirm).setOnClickListener {
            val label = dialog.findViewById<EditText>(R.id.et_label).text.toString()
            if (label.isNotBlank()) {

                val entity = PoiEntity(
                    id = 0,
                    longitude = initialPoint.longitude(),
                    latitude = initialPoint.latitude(),
                    label = label,
                    floor = selectedLocation.floorType.name
                )
                mViewModel.insertMarker(entity).observe(viewLifecycleOwner) {
                    selectedLocation.poiList.add(it)
                    addSingleMarker(selectedLocation, it, true, R.drawable.ic_marker)
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Please insert a label", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun addSingleMarker(
        location: MyLocation? = null,
        poiEntity: PoiEntity,
        draggable: Boolean,
        drawableId: Int? = null
    ): PointAnnotation? {
        val iconId = drawableId ?: R.drawable.ic_marker
        var annotation: PointAnnotation? = null
        bitmapFromDrawableRes(
            requireContext(),
            iconId
        )?.let { it ->

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                // Define a geographic coordinate.
                .withPoint(fromLngLat(poiEntity.longitude, poiEntity.latitude))
                // Specify the bitmap you assigned to the point annotation
                // The bitmap will be added to map style automatically.
                .withIconImage(it)
                .withData(JsonParser.parseString("${poiEntity.id}"))
            if (draggable)
                pointAnnotationOptions.withDraggable(true)

            if (poiEntity.label.isNotBlank()) {
                pointAnnotationOptions.withTextField(poiEntity.label)
                    .withTextSize(11.0)

                pointAnnotationOptions.textOffset = listOf(0.0, 1.5)
            }
            // Add the resulting pointAnnotation to the map.
            annotation = pointAnnotationManager.create(pointAnnotationOptions)

            location?.pointAnnotation?.add(annotation!!)
        }
        return annotation
    }

    override fun onAnnotationDrag(annotation: Annotation<*>) {
    }

    override fun onAnnotationDragFinished(annotation: Annotation<*>) {
        Coroutines.io {
            val point = (annotation as PointAnnotation).point
            annotation.getData()?.let {
                val id = it.asLong
                val poi = mViewModel.selectMarkerById(id)
                poi.longitude = point.longitude()
                poi.latitude = point.latitude()
                mViewModel.updateMarker(poi)
            }
        }
    }

    override fun onAnnotationDragStarted(annotation: Annotation<*>) {
    }


    override fun onRecycleItemClicked(entity: PoiEntity, action: Int) {
        super.onRecycleItemClicked(entity, action)
        selectedPoi = entity
        if (itineraryStartingPoint == null)
            Toast.makeText(
                requireContext(),
                "Choose a location from the point of interest list",
                Toast.LENGTH_LONG
            ).show()
        else {
            if (inItineraryMode) {
                drawItinerary(itineraryStartingPoint!!, selectedPoi!!)
                inItineraryMode = false
            }
        }
    }
}