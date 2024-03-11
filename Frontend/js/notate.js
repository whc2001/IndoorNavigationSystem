const MOUSE_LEFT = 1;
const MOUSE_MIDDLE = 2;
const MOUSE_RIGHT = 3;

const nodeTypes = {
    CONNECTOR: -1,
    CORRIDOR: 0,
    STAIR: 1,
    INSIDE_DOOR: 2,
    CLASSROOM: 3,
    ELEVATOR: 4,
    OUTSIDE_DOOR: 5,
};

const nodeColors = {
    [nodeTypes.CONNECTOR]: "cyan",
    [nodeTypes.CORRIDOR]: "red",
    [nodeTypes.STAIR]: "blue",
    [nodeTypes.INSIDE_DOOR]: "green",
    [nodeTypes.CLASSROOM]: "yellow",
    [nodeTypes.ELEVATOR]: "pink",
    [nodeTypes.OUTSIDE_DOOR]: "purple",
};

let btnOpenImage, btnExport;
let optEdit, optInspect, optAttribute;
let numGraphSize;
let optConnector, optCorridor, optStair, optInsideDoor, optClassroom, optElevator, optOutsideDoor;
let cvsMain, canvas;

let id = 0;
let isPanning = false;
let lastPanX, lastPanY;
let isConnecting = false;
let connectStartNode = null;
let connectWire = null;

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
        case optConnector.checked:
            return nodeTypes.CONNECTOR;
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
            return nodeTypes.CONNECTOR;
    }
}

function exportGraph() {
    const nodes = [];
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
            });
        }
    });
    return JSON.stringify(nodes, null, 2);
}

function placeNewNode(type, x, y) {
    const node = new fabric.Circle({
        radius: 10,
        fill: nodeColors[type],
        left: x,
        top: y,
        shadow: new fabric.Shadow({ 
            color: "gray", 
            blur: 2, 
            offsetX: 2, 
            offsetY: 2
        }),
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
    canvas.moveTo(node, 999);
}

function beginConnecting(startNode) {
    isConnecting = true;
    connectStartNode = startNode;
    connectWire = new fabric.Line([connectStartNode.left, connectStartNode.top, connectStartNode.left, connectStartNode.top], {
        stroke: "black",
        strokeWidth: 8,
        selectable: false,
        shadow: new fabric.Shadow({ 
            color: "gray", 
            blur: 2, 
            offsetX: 2, 
            offsetY: 2
        }),
    });
    connectWire.perPixelTargetFind = true;

    connectWire.graphProperties = {};
    connectWire.graphProperties.type = "edge";

    canvas.add(connectWire);
    canvas.moveTo(connectWire, -999);
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

function onCanvasMouseDown(o) {
    const operation = getSelectedOperation();
    const target = o.target;
    const pointer = canvas.getPointer(o.e);

    if(o.button === MOUSE_LEFT) {
        // if not dragging a node, start panning
        if(!target) {
            isPanning = true;
            lastPanX = o.e.clientX;
            lastPanY = o.e.clientY;
        }
    }
    else if(o.button === MOUSE_RIGHT) {
        if(operation === "edit") {
            if(target && o.e.ctrlKey) {
                const type = target.graphProperties.type;
                if (type === "node") {
                    deleteNode(target);
                }
                else if (type === "edge") {
                    deleteConnection(target);
                }
                canvas.renderAll();
            }
            else if(!target) {
                const type = getSelectedNodeType();
                placeNewNode(type, pointer.x, pointer.y);
                canvas.renderAll();
            }
            else if (target && target.graphProperties.type === "node" && !isConnecting) {
                beginConnecting(target);
                canvas.renderAll();
            }
        }
        else if (operation === "inspect") {
            if(target && target.graphProperties.type === "node") {
                alert(JSON.stringify(target, null, 2));
            }
        }
        else if(operation === "attribute") {
            if(target && target.graphProperties.type === "node") {
                const data = prompt("Enter attribute data", target.appProperties.data);
                if(data) {
                    target.appProperties.data = data;
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
    if(o.button === MOUSE_LEFT) {
        isPanning = false;
    }
    else if (o.button === MOUSE_RIGHT) {
        if (isConnecting) {
            endConnecting(o.target);
            canvas.renderAll();
        }
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

function onGraphSizeChange() {
    canvas.getObjects().forEach(obj => {
        if (obj.graphProperties?.type === "node") {
            obj.set({ radius: numGraphSize.value });
        }
        else if (obj.graphProperties?.type === "edge") {
            obj.set({ strokeWidth: numGraphSize.value / 1.25 });
        }
    });
    canvas.renderAll();
}

function initCanvas(width, height) {
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
}

function init() {
    btnOpenImage = document.getElementById("btnOpenImage");
    btnExport = document.getElementById("btnExport");

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
    cvsMain = document.getElementById("cvsMain");

    btnOpenImage.addEventListener("click", function () {
        const fileInput = document.createElement("input");
        fileInput.type = "file";
        fileInput.accept = "image/*";
        fileInput.onchange = function () {
            const file = fileInput.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    const img = new Image();
                    img.onload = function () {
                        initCanvas(cvsMain.clientWidth, cvsMain.clientHeight);
                        // make image centered on the canvas, and scale it to fit the canvas to show the whole image
                        const scale = Math.min(canvas.width / img.width, canvas.height / img.height);
                        canvas.setBackgroundImage(new fabric.Image(img), canvas.renderAll.bind(canvas), {
                            scaleX: scale,
                            scaleY: scale,
                            top: canvas.height / 2,
                            left: canvas.width / 2,
                            originX: "center",
                            originY: "center",
                            selectable: false,
                        });
                    };
                    img.src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        };
        fileInput.click();
    });

    btnExport.addEventListener("click", function () {
        const json = exportGraph();
        console.log(json);
        /*const blob = new Blob([json], { type: "application/json" });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "graph.json";
        a.click();
        */
    });
}

document.addEventListener("DOMContentLoaded", init);
