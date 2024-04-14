const MOUSE_LEFT = 1;
const MOUSE_MIDDLE = 2;
const MOUSE_RIGHT = 3;

const nodeTypes = {
    CORRIDOR: 0,
    STAIR: 1,
    INSIDE_DOOR: 2,
    CLASSROOM: 3,
    ELEVATOR: 4,
    OUTSIDE_DOOR: 5,
};

const nodeAttributeName = {
    [nodeTypes.CLASSROOM]: "classroom #",
    [nodeTypes.OUTSIDE_DOOR]: "node ID of outdoor map",
    [nodeTypes.ELEVATOR]: "node IDs of adjacent level elevator",
    [nodeTypes.STAIR]: "node IDs of adjacent level stair",
}

const nodeColors = {
    [nodeTypes.CORRIDOR]: "red",
    [nodeTypes.STAIR]: "blue",
    [nodeTypes.INSIDE_DOOR]: "green",
    [nodeTypes.CLASSROOM]: "yellow",
    [nodeTypes.ELEVATOR]: "pink",
    [nodeTypes.OUTSIDE_DOOR]: "purple",
};

const fileNamePattern = /^(?<building>[^\s]+)_(?<floor>[^\s]+)\..+$/s;

let grpCanvas;
let btnOpenImage, btnImportJSON, btnValidateGraph, btnExportJSON;
let txtBuilding, txtFloor;
let optEdit, optInspect, optAttribute;
let numGraphSize;
let optConnector, optCorridor, optStair, optInsideDoor, optClassroom, optElevator, optOutsideDoor;
let nodeTypeHotkeyTarget = [];
let cvsMain, canvas;
let image;

let id = 0;
let isPanning = false;
let lastPanX, lastPanY;
let isConnecting = false;
let connectStartNode = null;
let connectWire = null;
let graphSizeValue = 2;
let invalidNodes = new Set();
let invalidNodesFlashingTimer;
let invalidNodesFlashingState = false;

function recalculateGraphSize(scale) {
    graphSizeValue = Math.round(-20 * scale + 22.31);
    numGraphSize.value = graphSizeValue;
}

function getNodeSize() {
    return graphSizeValue;
}

function getConnectionSize() {
    return graphSizeValue / 1.25;
}

function highlightNode(node, highlight) {
    node.set({
        stroke: highlight ? "lawngreen" : "transparent",
        strokeWidth: highlight ? getConnectionSize() / 1.8 : 0,
    });
}

function markInvalidNode(node, invalid) {
    if (invalid) {
        invalidNodes.add(node);
    }
    else {
        invalidNodes.delete(node);
        highlightNode(node, false);
    }
}

function hasConnection(node1, node2) {  // check if two nodes are connected, ignore direction
    return node1.graphProperties.connections.some(i =>
        i.graphProperties.connects.includes(node1)
        && i.graphProperties.connects.includes(node2)
    ) || node2.graphProperties.connections.some(i =>
        i.graphProperties.connects.includes(node1)
        && i.graphProperties.connects.includes(node2)
    );
}

function getSelectedOperation() {
    switch (true) {
        case optEdit.checked:
            return "edit";
        case optInspect.checked:
            return "inspect";
        case optAttribute.checked:
            return "attribute";
        default:
            return "edit";
    }
}

function getSelectedNodeType() {
    switch (true) {
        case optCorridor.checked:
            return nodeTypes.CORRIDOR;
        case optStair.checked:
            return nodeTypes.STAIR;
        case optInsideDoor.checked:
            return nodeTypes.INSIDE_DOOR;
        case optClassroom.checked:
            return nodeTypes.CLASSROOM;
        case optElevator.checked:
            return nodeTypes.ELEVATOR;
        case optOutsideDoor.checked:
            return nodeTypes.OUTSIDE_DOOR;
        default:
            return nodeTypes.CORRIDOR;
    }
}

function importGraph(json) {
    // {"building":"ACW","floor":"1","nodes":[{"id":0,"type":0,"name":"Node0","coord":{"x":"855.1000","y":"544.5623"},"adjacents":[1]},{"id":1,"type":0,"name":"Node1","coord":{"x":"1125.5578","y":"1471.4686"},"adjacents":[0,2]},{"id":2,"type":0,"name":"Node2","coord":{"x":"1711.0544","y":"1064.4617"},"adjacents":[1,3,4]},{"id":3,"type":0,"name":"Node3","coord":{"x":"2522.4279","y":"654.4839"},"adjacents":[2,4]},{"id":4,"type":0,"name":"Node4","coord":{"x":"2873.1314","y":"1483.3521"},"adjacents":[3,2]}]}

    try {
        txtBuilding.value = json.building;
        txtFloor.value = json.floor;

        let maxNodeId = 0;

        json.nodes.forEach(node => {
            const newNode = placeNewNode(node.type, parseFloat(node.coord.x), parseFloat(node.coord.y));
            newNode.appProperties.id = node.id;
            newNode.appProperties.name = node.name;
            newNode.appProperties.data = node.data;
            maxNodeId = Math.max(maxNodeId, node.id);
        });

        const nodeDict = {};
        canvas.getObjects().forEach(obj => {
            if (obj.graphProperties?.type === "node") {
                nodeDict[obj.appProperties.id] = obj;
            }
        });
        json.nodes.forEach(node => {
            const currentNode = nodeDict[node.id];
            node.adjacents.forEach(adjacentId => {
                const adjacentNode = nodeDict[adjacentId];
                if (adjacentNode) {
                    beginConnecting(currentNode);
                    endConnecting(adjacentNode);
                }
            });
        });

        id = maxNodeId + 1;
    }
    catch (e) {
        alert("Import failed: " + e);
        canvas.remove(...canvas.getObjects());
    }
}

function checkGraph() {
    let errorMsg = "";

    // Check there is no isolated node
    let isolatedNode = false;
    canvas.getObjects().forEach(obj => {
        if (obj.graphProperties?.type === "node" && obj.graphProperties.connections.length === 0) {
            markInvalidNode(obj, true);
            isolatedNode = true;
        }
    });
    if (isolatedNode)
        errorMsg += "Some nodes are isolated\n";

    // Check if all special nodes have attributes
    let missingAttribute = false;
    canvas.getObjects().forEach(obj => {
        if (obj.graphProperties?.type === "node") {
            if (nodeAttributeName[obj.appProperties.type] && !obj.appProperties.data) {
                markInvalidNode(obj, true);
                missingAttribute = true;
            }
        }
    });
    if (missingAttribute)
        errorMsg += "Some nodes are missing mandatory attributes\n";

    return errorMsg.length > 0 ? errorMsg : null;
}

function exportGraph() {
    const building = txtBuilding.value.trim();
    if (!building) {
        alert("Please enter building!");
        return;
    }
    const floor = txtFloor.value.trim();
    if (!floor) {
        alert("Please enter floor!");
        return;
    }

    // Check if graph is valid, but do not disallow export
    const errorMsg = checkGraph();
    if (errorMsg) {
        const confirmed = confirm(errorMsg + "\nDo you want to export anyway?");
        if (!confirmed)
            return;
    }

    let ret = {};
    ret["building"] = building;
    ret["floor"] = floor;

    let nodes = [];
    canvas.getObjects().forEach(obj => {
        if (obj.graphProperties?.type === "node") {
            nodes.push({
                id: obj.appProperties.id,
                type: obj.appProperties.type,
                name: obj.appProperties.name,
                coord: {
                    x: obj.left.toFixed(4),
                    y: obj.top.toFixed(4),
                },
                adjacents: obj.graphProperties.connections.map(line => {
                    const peer = line.graphProperties.connects.find(i => i !== obj);
                    return peer.appProperties.id;
                }),
                data: obj.appProperties.data,
            });
        }
    });

    ret["nodes"] = nodes;

    return JSON.stringify(ret, null, 2);
}

function placeNewNode(type, x, y) {
    const node = new fabric.Circle({
        radius: getNodeSize(),
        fill: nodeColors[type],
        left: x,
        top: y,
        lockMovementX: true,
        lockMovementY: true,
        // shadow: new fabric.Shadow({
        //     color: "gray",
        //     blur: 2,
        //     offsetX: 2,
        //     offsetY: 2
        // }),
    });
    node.hasControls = false;
    node.hasBorders = false;

    node.graphProperties = {};
    node.graphProperties.type = "node";
    node.graphProperties.connections = [];
    node.appProperties = {};
    node.appProperties.id = id++;
    node.appProperties.name = "Node " + node.appProperties.id;
    node.appProperties.type = type;

    node.on("moving", function () {
        this.graphProperties.connections.forEach(line => {
            if (line.graphProperties.connects[0] === this) {
                line.set({ x1: this.left, y1: this.top });
                line.setCoords();
            }
            else if (line.graphProperties.connects[1] === this) {
                line.set({ x2: this.left, y2: this.top });
                line.setCoords();
            }
        });
        canvas.renderAll();
    });
    canvas.add(node);
    canvas.bringToFront(node);

    return node;
}

function beginConnecting(startNode) {
    isConnecting = true;
    connectStartNode = startNode;
    connectWire = new fabric.Line([connectStartNode.left, connectStartNode.top, connectStartNode.left, connectStartNode.top], {
        stroke: "black",
        strokeWidth: getConnectionSize(),
        selectable: false,
        // shadow: new fabric.Shadow({
        //     color: "gray",
        //     blur: 2,
        //     offsetX: 2,
        //     offsetY: 2
        // }),
    });
    connectWire.perPixelTargetFind = true;

    connectWire.graphProperties = {};
    connectWire.graphProperties.type = "edge";

    canvas.add(connectWire);
    canvas.sendToBack(connectWire);
}

function endConnecting(endNode) {
    if (!isConnecting) return;

    // Only end connecting if connection in progress and finish on another different node
    if (endNode && endNode != connectStartNode) {
        const connectEndNode = endNode;
        connectWire.set({ x2: connectEndNode.left, y2: connectEndNode.top });
        connectWire.graphProperties.connects = [connectStartNode, connectEndNode];
        // Avoid duplicate connections between two nodes
        if (hasConnection(connectStartNode, connectEndNode)) {
            canvas.remove(connectWire);
            console.log("connect failed: duplicate connection");
        }
        else {
            connectWire.setCoords();
            connectStartNode.graphProperties.connections.push(connectWire);
            connectEndNode.graphProperties.connections.push(connectWire);
            console.log("connect success");
            markInvalidNode(connectStartNode, false);
            markInvalidNode(connectEndNode, false);
        }
    }
    // Not finishing on another node
    else {
        canvas.remove(connectWire);
        console.log("connect failed: no target node");
    }
    connectWire = null;
    isConnecting = false;
    connectStartNode = null;
}

function deleteNode(node) {
    if (!node) return;

    // Remove all connections related to this node
    if (node.graphProperties.connections.length > 0) {
        node.graphProperties.connections.forEach(line => {
            // Remove the relationship from the peer node
            line.graphProperties.connects.forEach(connectNode => {
                connectNode.graphProperties.connections = connectNode.graphProperties.connections.filter(i =>
                    i !== line
                );
            });
            canvas.remove(line);
        });
    }
    canvas.remove(node);
}

function deleteConnection(edge) {
    if (!edge) return;

    // Remove the relationship from the peer nodes
    edge.graphProperties.connects.forEach(connectNode => {
        connectNode.graphProperties.connections = connectNode.graphProperties.connections.filter(i =>
            i !== edge
        );
    });
    canvas.remove(edge);
}

function resizeGraph() {
    if (canvas) {
        canvas.getObjects().forEach(obj => {
            if (obj.graphProperties?.type === "node") {
                obj.set({ radius: getNodeSize() });
                obj.setCoords();
            }
            else if (obj.graphProperties?.type === "edge") {
                obj.set({ strokeWidth: getConnectionSize() });
                obj.setCoords();
            }
        });
        canvas.renderAll();
    }
}

function setNodeMovable(canMove) {
    canvas.getObjects().forEach(obj => {
        if (obj.graphProperties?.type === "node") {
            obj.lockMovementX = !canMove;
            obj.lockMovementY = !canMove;
        }
    });
}

function onCanvasMouseDown(o) {
    const operation = getSelectedOperation();
    const target = o.target;
    const pointer = canvas.getPointer(o.e);

    if (o.button === MOUSE_RIGHT) {
        if (!target) {
            isPanning = true;
            lastPanX = o.e.clientX;
            lastPanY = o.e.clientY;
        }
        else {
            if (operation === "edit" && target && target.graphProperties.type === "node" && !isConnecting) {
                beginConnecting(target);
                canvas.renderAll();
            }
            else if (operation === "inspect") {
                if (target && target.graphProperties.type === "node") {
                    alert(JSON.stringify(target.appProperties, null, 2));
                }
            }
            else if (operation === "attribute") {
                if (target && target.graphProperties.type === "node") {
                    console.log(target.appProperties.type);
                    const attributeName = nodeAttributeName[target.appProperties.type];
                    console.log(attributeName);
                    if (attributeName) {
                        const data = prompt("Enter attribute data: " + nodeAttributeName[target.appProperties.type], target.appProperties.data);
                        if (data) {
                            target.appProperties.data = data;
                            markInvalidNode(target, false);
                        }
                    }
                }
            }
        }
    }
    else if (o.button === MOUSE_LEFT) {
        if (operation === "edit") {
            if (target && o.e.ctrlKey) {
                const type = target.graphProperties.type;
                if (type === "node") {
                    deleteNode(target);
                }
                else if (type === "edge") {
                    deleteConnection(target);
                }
                canvas.renderAll();
            }
            else if(target && o.e.shiftKey) {   // Change node type
                const newType = getSelectedNodeType();
                if(target.graphProperties.type === "node") {
                    target.set({ fill: nodeColors[newType] });
                    target.appProperties.type = newType;
                    if(!nodeAttributeName[newType])
                        delete target.appProperties.data;
                    canvas.renderAll();
                }
            }
            else if (!target) {
                if (pointer.x >= 0 && pointer.y >= 0 && pointer.x <= canvas.backgroundImage.width && pointer.y <= canvas.backgroundImage.height) {
                    const type = getSelectedNodeType();
                    placeNewNode(type, pointer.x, pointer.y);
                    canvas.renderAll();
                }
            }
        }
    }
}

function onCanvasMouseMove(o) {
    const pointer = canvas.getPointer(o.e);

    // Update connecting wire if connection in progress
    if (isConnecting) {
        connectWire.set({ x2: pointer.x, y2: pointer.y });
        canvas.renderAll();
    }
    else if (isPanning) {
        const deltaX = o.e.clientX - lastPanX;
        const deltaY = o.e.clientY - lastPanY;
        lastPanX = o.e.clientX;
        lastPanY = o.e.clientY;
        canvas.relativePan(new fabric.Point(deltaX, deltaY));
    }
}

function onCanvasMouseUp(o) {
    if (o.button === MOUSE_RIGHT) {
        if (isConnecting) {
            endConnecting(o.target);
            canvas.renderAll();
        }
        else {
            isPanning = false;
        }
    }
}

function onCanvasObjectMoving(o) {
    // Limit node movement within background image
    const obj = o.target;
    if (obj.graphProperties?.type === "node") {
        if (obj.left < 0) obj.left = 0;
        if (obj.top < 0) obj.top = 0;
        if (obj.left > canvas.backgroundImage.width) obj.left = canvas.backgroundImage.width;
        if (obj.top > canvas.backgroundImage.height) obj.top = canvas.backgroundImage.height;
    }
}

function onMouseWheel(o) {
    var delta = o.e.deltaY;
    var zoom = canvas.getZoom();
    zoom *= (o.e.shiftKey ? 0.99 : 0.999) ** delta;
    if (zoom > 20) zoom = 20;
    if (zoom < 0.01) zoom = 0.01;
    canvas.zoomToPoint({ x: o.e.offsetX, y: o.e.offsetY }, zoom);
    o.e.preventDefault();
    o.e.stopPropagation();
}


function onKeyDown(o) {
    if (getSelectedOperation() === "edit" && o.keyCode === 18) {
        setNodeMovable(true);
    }
}

function onKeyUp(o) {
    if (o.keyCode === 18) {
        setNodeMovable(false);
    }
}

function onKeyPress(o) {
    if (document.activeElement === document.body) { // Not focused on textbox
        if (o.keyCode >= 49 && o.keyCode <= 54) {
            const id = o.keyCode - 49;
            if (nodeTypeHotkeyTarget[id]) {
                nodeTypeHotkeyTarget[id].click();
            }
        }
    }
}

function onGraphSizeChange() {
    graphSizeValue = numGraphSize.value;
    resizeGraph();
}

function initCanvas(width, height) {
    if(canvas)
        canvas.dispose();
    canvas = new fabric.Canvas("cvsMain", {
        width: width,
        height: height,
        fireRightClick: true,
        fireMiddleClick: true,
        stopContextMenu: true,
    });
    canvas.selection = false;
    fabric.Object.prototype.originX = "center";
    fabric.Object.prototype.originY = "center";

    canvas.on("mouse:down", onCanvasMouseDown);
    canvas.on("mouse:move", onCanvasMouseMove);
    canvas.on("mouse:up", onCanvasMouseUp);
    canvas.on("mouse:wheel", onMouseWheel);
    canvas.on("object:moving", onCanvasObjectMoving);

    //grpCanvas.tabIndex = 1000;
    document.addEventListener("keydown", onKeyDown, false);
    document.addEventListener("keyup", onKeyUp, false);
    document.addEventListener("keypress", onKeyPress, false);

}

function onLoadImage() {
    initCanvas(grpCanvas.clientWidth, grpCanvas.clientHeight);

    canvas.setBackgroundImage(new fabric.Image(image), canvas.renderAll.bind(canvas), {
        scaleX: 1,
        scaleY: 1,
        left: 0,
        top: 0,
        originX: 'left',
        originY: 'top',
        selectable: false,
    });

    const scale = Math.min(grpCanvas.clientWidth / image.width, grpCanvas.clientHeight / image.height);
    const translateX = (grpCanvas.clientWidth - image.width * scale) / 2;
    const translateY = (grpCanvas.clientHeight - image.height * scale) / 2;
    canvas.setViewportTransform([scale, 0, 0, scale, translateX, translateY]);

    recalculateGraphSize(scale);

    id = 0;
    isPanning = false;
    isConnecting = false;
    connectStartNode = null;
    connectWire = null;
    invalidNodes = new Set();
    invalidNodesFlashingTimer = null;
    invalidNodesFlashingState = false;

    btnImportJSON.removeAttribute("disabled");
    btnValidateGraph.removeAttribute("disabled");
    btnExportJSON.removeAttribute("disabled");
    txtBuilding.removeAttribute("disabled");
    txtFloor.removeAttribute("disabled");
}

function init() {
    grpCanvas = document.getElementById("grpCanvas");

    btnOpenImage = document.getElementById("btnOpenImage");
    btnImportJSON = document.getElementById("btnImportJSON");
    btnValidateGraph = document.getElementById("btnValidateGraph");
    btnExportJSON = document.getElementById("btnExportJSON");

    txtBuilding = document.getElementById("txtBuilding");
    txtFloor = document.getElementById("txtFloor");

    optEdit = document.getElementById("optEdit");
    optInspect = document.getElementById("optInspect");
    optAttribute = document.getElementById("optAttribute");

    numGraphSize = document.getElementById("numGraphSize");
    numGraphSize.addEventListener("change", onGraphSizeChange);

    optConnector = document.getElementById("optConnector");
    optCorridor = document.getElementById("optCorridor");
    optStair = document.getElementById("optStair");
    optInsideDoor = document.getElementById("optInsideDoor");
    optClassroom = document.getElementById("optClassroom");
    optElevator = document.getElementById("optElevator");
    optOutsideDoor = document.getElementById("optOutsideDoor");
    nodeTypeHotkeyTarget = [optCorridor, optInsideDoor, optClassroom, optStair, optOutsideDoor, optElevator];

    cvsMain = document.getElementById("cvsMain");

    invalidNodesFlashingTimer = setInterval(() => {
        if(invalidNodes.size === 0) return;
        invalidNodesFlashingState = !invalidNodesFlashingState;
        invalidNodes.forEach(node => {
            highlightNode(node, invalidNodesFlashingState);
        });
        canvas.renderAll();
    }, 500);

    btnOpenImage.addEventListener("click", function () {
        const fileInput = document.createElement("input");
        fileInput.type = "file";
        fileInput.accept = "image/*";
        fileInput.onchange = function () {
            const file = fileInput.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    // Load image to canvas
                    image = new Image();
                    image.onload = onLoadImage;
                    image.src = e.target.result;

                    const fileNameMatch = file.name.match(fileNamePattern);
                    if (!fileNameMatch) {
                        alert('Filename is not in "Building_Floor" format, please manually specify');
                    }
                    else {
                        txtBuilding.value = fileNameMatch.groups.building;
                        txtFloor.value = fileNameMatch.groups.floor;
                    }
                };
                reader.readAsDataURL(file);
            }
        };
        fileInput.click();
    });

    btnImportJSON.addEventListener("click", function () {
        if (canvas && canvas.getObjects().length > 0) {
            if (!confirm("This will clear the current graph, continue?")) {
                return;
            }
        }

        const fileInput = document.createElement("input");
        fileInput.type = "file";
        fileInput.accept = "application/json";
        fileInput.onchange = function () {
            const file = fileInput.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    let json;
                    try {
                        json = JSON.parse(e.target.result);
                    }
                    catch {
                        alert("JSON parsing failed!");
                        return;
                    }
                    canvas.remove(...canvas.getObjects());
                    importGraph(json);
                    canvas.renderAll();
                };
                reader.readAsText(file);
            }
        };
        fileInput.click();
    });

    btnValidateGraph.addEventListener("click", function () {
        if(!canvas || canvas.getObjects().length === 0)
            return alert("❌Graph is empty!");
        const errorMsg = checkGraph();
        if (errorMsg)
            alert("❌Problem detected:\n" + errorMsg);
        else
            alert("✔Graph is valid!");
    });

    btnExportJSON.addEventListener("click", function () {
        const json = exportGraph();
        if (json) {
            const blob = new Blob([json], { type: "application/json" });
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = `${txtBuilding.value.trim()}_${txtFloor.value.trim()}.json`;
            a.click();
        }
    });
}

window.addEventListener('beforeunload', function (e) {
    if (canvas) {
        e.preventDefault();
        return "";  // default message
    }
});
document.addEventListener("DOMContentLoaded", init);
