let url = window.location.href;

let versionPart = /.*\d+\.\d+(\.\d+)?([^\/]+)?/.exec(url)[0];

$(document).ready(function () {
    $.ajax({
        url: versionPart + "/search-index.json",
        dataType: "json"
    }).done(function (data) {
        $('#searchBar').autocomplete({
            delay: 400,
            minLength: 2,
            source: data,
            position: {my: "right top", at: "right bottom"},
            autoFocus: true,
            select: function (event, ui) {
                if (ui.item != null) {
                    window.location.href = versionPart + "/" + ui.item.value;
                }
            }
        }).autocomplete( "instance" )._renderItem = function(ul, item) {
            return $('<li>')
                .append('<div>' + item.label.substring(item.label.lastIndexOf('.') + 1) + '<br>' +
                    '<span class="small-desc">Package: ' + item.label.substring(0, item.label.lastIndexOf('.')) + '</span>' +
                    '</div>')
                .appendTo(ul);
        };
    });
});

$(window).on("pageshow", function () {
    $('#searchBar').val('');
});