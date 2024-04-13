var isProduction = false;
var apiServer = isProduction ? "https://indoornav.haoc.wang" : "http://127.0.0.1:4090";

var button = document.getElementById('jumpButton');1

button.addEventListener('click', function () {
    var targetUrl = apiServer+'/api/apiPage';

    window.location.href = targetUrl;
});


const imgElement = document.getElementById('mapImage');

var currentWidth = document.documentElement.clientWidth;

imgElement.style.width = currentWidth / 1.4 + "px";

$(document).ready(function () {
    var List;
    $.ajax(
        {
            url: apiServer +  "/api/GetBuildingList",
            type: "GET",
            success: function (data) {

                var buildingList = data;
                console.log(buildingList);
                console.log(data);
                List = buildingList;
                var addedOptions = [];
                var select = document.getElementsByName("name")[0];
                var select2 = document.getElementsByName("name")[1];
                var select3 = document.getElementsByName("name")[2];
                for (var i = 0; i < buildingList.length; i++) {
                    if (addedOptions.indexOf(buildingList[i].name) === -1&&buildingList[i].name!=="Campus") {
                        var option = document.createElement("option");
                        option.text = buildingList[i].name;
                        option.value = buildingList[i].name;
                        select.appendChild(option);
                        var option = document.createElement("option");
                        option.text = buildingList[i].name;
                        option.value = buildingList[i].name;
                        select2.appendChild(option);
                        var option = document.createElement("option");
                        option.text = buildingList[i].name;
                        option.value = buildingList[i].name;
                        select3.appendChild(option);
                        addedOptions.push(buildingList[i].name);
                    }
                }
            },
            error: function () {
                alert("Error");
            }
        }
    );
    $("#nameSelect").on("change", function () {
        var selectedBuilding = $(this).val();
        var floorSelect = $("#floorSelect");

        var filteredFloors = List.filter(function (item) {
            return item.name === selectedBuilding;
        });

        filteredFloors.sort(function(a, b) {
            return a.floor - b.floor;
        });

        floorSelect.empty();
        floorSelect.append("<option value=''>Select Floor</option>");
        for (var i = 0; i < filteredFloors.length; i++) {
            var option = document.createElement("option");
            option.text = filteredFloors[i].floor;
            option.value = filteredFloors[i].floor;
            floorSelect.append(option);
        }
    });

    $("#nameSelect2").on("change", function () {
        var selectedBuilding = $(this).val();
        var floorSelect = $("#floorSelect2");

        var filteredFloors = List.filter(function (item) {
            return item.name === selectedBuilding;
        });

        floorSelect.empty();
        floorSelect.append("<option value=''>Select Floor</option>");
        for (var i = 0; i < filteredFloors.length; i++) {
            var option = document.createElement("option");
            option.text = filteredFloors[i].floor;
            option.value = filteredFloors[i].floor;
            floorSelect.append(option);
        }
    });
});

document.getElementById('NavigateButton').addEventListener('click', function (event) {
    event.preventDefault();
    var formData = {
        name: $("#nameSelect2").val(),
        name2: $("#nameSelect3").val(),
        start: $("#start").val(),
        end: $("#end").val()
    };
    var container = document.getElementById('imageContainer2');

    $.ajax({
        url: apiServer + '/api/Navigate',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            if (response == null || response.length === 0) {
                alert('Wrong input, please enter again.');
                return;
            }

            container.innerHTML = "";

            var mainImage = document.getElementById('mapImage');
            mainImage.src = 'data:image/jpeg;base64,' + response[0];

            var description = document.createElement('p');
            description.textContent = 'This is the route you have selected.';
            container.appendChild(description);

            response.forEach(function (base64Image) {
                var imgElement = document.createElement('img');
                imgElement.src = 'data:image/jpeg;base64,' + base64Image;
                imgElement.style.width = "150px";
                imgElement.style.height = "150px";
                imgElement.style.margin = "10px";
                imgElement.style.marginTop = "3px";
                imgElement.addEventListener('click', function () {
                    var imgElement = document.getElementById('mapImage');
                    imgElement.src = 'data:image/jpeg;base64,' + base64Image;
                });

                var thumbnailDiv = document.createElement('div');
                thumbnailDiv.style.display = 'inline-block';
                thumbnailDiv.appendChild(imgElement);


                container.appendChild(thumbnailDiv);
            });
        },
        error: function (request, msg, error) {
            console.error('Error:', error);
        }
    });
});


document.getElementById('LoadButton').addEventListener('click', function (event) {
    event.preventDefault();
    var nameSelect = document.getElementById('nameSelect');
    var floorSelect = document.getElementById('floorSelect');

    if (nameSelect.value !== '' && floorSelect.value !== '') {
        var formData = {
            name: $("#nameSelect").val(),
            floor: $("#floorSelect").val(),
            start: 0,
            end: 0
        };
        var imgElement = document.getElementById('mapImage');

        $.ajax({
            url: apiServer + '/api/Load',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function (response) {
                var base64Image = 'data:image/jpeg;base64,' + response;
                imgElement.src = base64Image;
                var container = document.getElementById('imageContainer2');
                var thumbnailDiv = document.createElement('div');
                thumbnailDiv.style.display = 'inline-block';
                var specificImageId = $("#nameSelect").val() + $("#floorSelect").val() + 'Load';

                var imagesInContainer = container.getElementsByTagName('img');

                var isImageExist = false;

                for (var i = 0; i < imagesInContainer.length; i++) {
                    var image = imagesInContainer[i];
                    if (image.id === specificImageId) {
                        isImageExist = true;
                        break;
                    }
                }
                if (isImageExist) {
                    var image = document.getElementById(specificImageId);
                    image.src = base64Image;
                } else {
                    var newImage = document.createElement('img');
                    newImage.src = base64Image;
                    newImage.style.cssText = "inline-block;";
                    newImage.id = $("#nameSelect").val() + $("#floorSelect").val() + 'Load';
                    newImage.style.width = "150px";
                    newImage.style.height = "150px";
                    newImage.style.margin = "10px";
                    newImage.style.marginTop = "3px";
                    newImage.addEventListener('click', function () {
                        var imgElement = document.getElementById('mapImage');
                        imgElement.src = base64Image;
                    });
                    thumbnailDiv.appendChild(newImage);
                    var description = document.createElement('p');
                    description.textContent = $("#nameSelect").val() + ' Floor ' + $("#floorSelect").val() + ' Map';
                    thumbnailDiv.appendChild(description);
                    container.appendChild(thumbnailDiv);
                }
            },
            error: function (request, msg, error) {

            }
        });
    } else {
        alert('Please select both a building and a floor before loading.');
    }
});

window.onscroll = function () {
    var scrollToTopButton = document.getElementById("scrollToTop");
    var scrollToBottomButton = document.getElementById("scrollToBottom");
    var windowHeight = window.innerHeight;
    var bodyHeight = document.body.scrollHeight;
    if (document.documentElement.scrollTop > 0) {
        scrollToTopButton.style.display = "block";
    } else {
        scrollToTopButton.style.display = "none";
    }
    if (document.documentElement.scrollTop + windowHeight >= bodyHeight) {
        scrollToBottomButton.style.display = "none";
    } else {
        scrollToBottomButton.style.display = "block";
    }
};

function scrollToTop() {

    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

function scrollToBottom() {
    var bodyHeight = document.body.scrollHeight;

    var scrollDistance = bodyHeight;

    window.scroll({
        top: scrollDistance,
        behavior: 'smooth'
    });
}

$(document).ready(function() {
    $('#nameSelect, #floorSelect').change(function() {
        var selectedBuilding = $('#nameSelect').val();
        var selectedFloor = $('#floorSelect').val();

        if (selectedBuilding !== '' && selectedFloor !== '') {
            console.log('fetching rooms');
            fetchAndDisplayRooms(selectedBuilding, selectedFloor);
        }
    });
    function fetchAndDisplayRooms(building, floor) {
        var roomsData ;
        var formData = {
            name: building,
            floor: floor,
        };
        $.ajax({
            url: apiServer + '/api/GetRooms',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function(data) {
                updateRoomsTable(data);
            },
            error: function() {
                alert('Error fetching rooms data');
            }
        });


    }
    function updateRoomsTable(rooms) {
        $('#accessibleRoomsTable tbody').empty();

        var columnsPerRow = 5;

        var numRows = Math.ceil(rooms.length / columnsPerRow);

        for (var rowIndex = 0; rowIndex < numRows; rowIndex++) {
            var rowHtml = '<tr>';

            for (var colIndex = 0; colIndex < columnsPerRow; colIndex++) {
                var roomIndex = rowIndex * columnsPerRow + colIndex;

                if (roomIndex < rooms.length) {
                    rowHtml += '<td>' + rooms[roomIndex] + '</td>';
                } else {
                    rowHtml += '<td></td>';
                }
            }

            rowHtml += '</tr>';

            $('#accessibleRoomsTable tbody').append(rowHtml);
        }
    }
});

document.getElementById('scrollToBottom').addEventListener('click', scrollToBottom);
