function fctWaiting(show) {
	if (show) {
		// Pour IE8
		if ($('#waiting-inner-ie8').length > 0) {
			$('#waiting').css("background", "background-color: #ffffff");
			$('#waiting-inner-ie8').html('<img src="../images/spinner.gif"/>');
		} else {
			// $('#waiting').css("background", "rgb(255, 255, 255) url('../images/waiting.gif') 50% 50% no-repeat");
		}

		$('body').addClass('loading');
	} else {
		$('body').removeClass('loading');
	}
}