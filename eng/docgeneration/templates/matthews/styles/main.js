// Use container fluid
var containers = $(".container");
containers.removeClass("container");
containers.addClass("container-fluid");

var SELECTED_LANGUAGE = 'java'

// Navbar Hamburger
$(function () {
    $(".navbar-toggle").click(function () {
        $(this).toggleClass("change");
    })
})

// Select list to replace affix on small screens
$(function () {
    var navItems = $(".sideaffix .level1 > li");

    if (navItems.length == 0) {
        return;
    }

    var selector = $("<select/>");
    selector.addClass("form-control visible-sm visible-xs");
    var form = $("<form/>");
    form.append(selector);
    form.prependTo("article");

    selector.change(function () {
        window.location = $(this).find("option:selected").val();
    })

    function work(item, level) {
        var link = item.children('a');

        var text = link.text();

        for (var i = 0; i < level; ++i) {
            text = '&nbsp;&nbsp;' + text;
        }

        selector.append($('<option/>', {
            'value': link.attr('href'),
            'html': text
        }));

        var nested = item.children('ul');

        if (nested.length > 0) {
            nested.children('li').each(function () {
                work($(this), level + 1);
            });
        }
    }

    navItems.each(function () {
        work($(this), 0);
    });
})

// Inject line breaks and spaces into the code sections
$(function () {
    $(".lang-csharp").each(function () {
        var text = $(this).html();
        text = text.replace(/, /g, ",</br>&#32;&#32;&#32;&#32;&#32;&#32;&#32;&#32;");
        $(this).html(text);
    });
})

function httpGetAsync(targetUrl, callback) {
    console.log(targetUrl);
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText);
    }
    xmlHttp.open("GET", targetUrl, true); // true for asynchronous 
    xmlHttp.send(null);
}

function populateIndexList(selector, packageName) {
    var url = "https://azuresdkdocs.blob.core.windows.net/$web/" + SELECTED_LANGUAGE + "/" + packageName + "/versioning/versions"
    var gaCollapsible = $('<div class="ga versionarrow">&nbsp;&nbsp;&nbsp;GA versions</div>')
    var previewCollapsible = $('<div class="preview versionarrow">&nbsp;&nbsp;&nbsp;Preview versions</div>')
    var gaPublishedVersions = $('<ul style="display: none;"></ul>')
    var previewPublishedVersions = $('<ul style="display: none;"></ul>')
    $(selector).after(gaCollapsible)
    $(gaCollapsible).after(gaPublishedVersions)
    $(gaPublishedVersions).after(previewCollapsible)
    $(previewCollapsible).after(previewPublishedVersions)
    var collapsibleFunc = function(collapsible, publishedVersions) {
        $(collapsible).on('click', function(event) {
            event.preventDefault();
            if (collapsible.hasClass('disable')) {
                return
            }
            $(this).toggleClass('down')
            if ($(this).hasClass('down')) {
                if (!$(selector).hasClass('loaded')){
                    httpGetAsync(url, function (responseText) {
                        if (responseText) {
                            options = responseText.match(/[^\r\n]+/g)
                            for (var i in options) {
                                if (options[i].indexOf('beta') >= 0) {
                                    $(previewPublishedVersions).append('<li><a href="' + getPackageUrl(SELECTED_LANGUAGE, packageName, options[i]) + '" target="_blank">' + packageName + ' - ' + options[i] + '</a></li>')
                                } else {
                                    $(gaPublishedVersions).append('<li><a href="' + getPackageUrl(SELECTED_LANGUAGE, packageName, options[i]) + '" target="_blank">' + packageName + ' - ' + options[i] + '</a></li>')
                                }
                            }
                        }
                        else {
                            $(publishedVersions).append('<li>No discovered versions present in blob storage.</li>')
                        }                
                        $(selector).addClass("loaded")

                      if ($(previewPublishedVersions).children().length == 0) {
                            $(previewCollapsible).addClass('disable')
                        }
                      if ($(gaPublishedVersions).children().length == 0) {
                            $(gaCollapsible).addClass('disable')
                        }
                    })
                }
                $(publishedVersions).show()
            } else {
                $(publishedVersions).hide()
            }
        });
    }

    collapsibleFunc($(gaCollapsible), $(gaPublishedVersions))
    collapsibleFunc($(previewCollapsible), $(previewPublishedVersions))
}

function getPackageUrl(language, package, version) {
    return "https://azuresdkdocs.blob.core.windows.net/$web/" + language + "/" + package + "/" + version + "/index.html"
}

// Populate Index
$(function () {
    $('h4').each(function () {
        var pkgName = $(this).text()
        populateIndexList($(this), pkgName)
    });
})