let url = window.location.href;
let versionPart = /.*\d+\.\d+(\.\d+)?([^\/]+)?/.exec(url)[0];

$(document).ready(function () {
    $.ajax({
        url: versionPart + "/search-index.json",
        dataType: "json"
    }).done(function (data) {
        let defMapping = data['default-mapping'];
        let index = data['index'];

        let localMapping = localStorage.getItem('search-default-mapping-' + versionPart) || defMapping;

        if (!index.hasOwnProperty(localMapping)) {
            localMapping = defMapping;
        }

        $('#searchBar').autocomplete({
            delay: 400,
            minLength: 2,
            source: index[localMapping],
            position: {my: "right top", at: "right bottom"},
            autoFocus: true,
            select: function (event, ui) {
                if (ui.item != null) {
                    window.location.href = versionPart + "/" + ui.item.value;
                }
            }
        }).autocomplete( "instance" )._renderItem = function(ul, item) {
            let packageName = item.label.substring(0, item.label.lastIndexOf('.'));
            return $('<li>')
                .append('<div>' + item.label.substring(item.label.lastIndexOf('.') + 1) + '<br>' +
                    (packageName ? '<span class="small-desc">Package: ' + packageName + '</span>' : '') +
                    '</div>')
                .appendTo(ul);
        };

        let select = $('#searchBarMappingSelect');

        for (const key in index) {
            select.append('<option ' + (key === localMapping ? 'selected' : '') + ' value="' + key +'">' + key.charAt().toUpperCase() + key.substring(1).toLowerCase() + '</option>');
        }

        select.on('change', function (e) {
            let valueSelected = this.value;
            $('#searchBar').autocomplete('option', 'source', index[valueSelected]);
            localStorage.setItem('search-default-mapping-' + versionPart, valueSelected);
        });
    });
});

$(window).on("pageshow", function () {
    $('#searchBar').val('');

    hljs.highlightAll();
});