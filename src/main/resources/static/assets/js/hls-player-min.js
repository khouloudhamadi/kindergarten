function initPage() {
    var urlServer = "http://localhost/temp/hls";
    var player = videojs('remoteVideo');
    var applyFn = function () {
        psn = document.getElementById("keystream").value;
        var streamName = psn;
        streamName = encodeURIComponent(streamName);
        var src = urlServer + "/"  + streamName + ".m3u8";
        player.src({
            src: src,
            type: "application/vnd.apple.mpegurl"
        });
        player.play();
    };
    applyBtn.onclick = applyFn()
}