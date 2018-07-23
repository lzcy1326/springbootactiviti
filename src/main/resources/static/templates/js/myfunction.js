function startprocessDefinition(processDefinitionId) {
	var json = {};
	$("#paramtable").find("tr").each(function(i, e){
		if(e.id != "row_0"){
			var key = $(e).find("[name='key']").val();
			var value = $(e).find("[name='value']").val();
			var data = {};
			json[key] = value;
		}
	});
	$.ajax({
		url : basePath + "/processdefinition/start/" + processDefinitionId,
		type: "post",
		data: json,
		success : function(data) {
			alert('started');
			window.location.href = basePath + data;
			$("#errortext").html("");
		},
		error : function(e){
			$("#errortext").html(e.responseText);
		}
	});
}

function deleteprocessDefinition(processDefinitionId) {
	$.ajax({
		url : basePath + "/processdefinition/delete/" + processDefinitionId,
		type: "post",
		success : function(data) {
			alert('deleted');
			window.location.href = basePath + data;
			$("#errortext").html("");
		},
		error : function(e){
			$("#errortext").html(e.responseText);
		}
	});
}

function showbiggerFlow(processDefinitionId, isSvg){
	if(isSvg){
		$("#flowimg").attr("src", 
				basePath + "/diagram-viewer/index.html?processDefinitionId=" + processDefinitionId + "&_date="+new Date().getTime());
	}else{
		$("#flowimg").attr("src", basePath + "processdefinition/querypng/" + processDefinitionId + "?_date="+new Date().getTime());
	}
	$("#biggerimg").modal("show");
}

function showbiggerTrackFlow(processDefinitionId, processInstanceId, isSvg){
	if(isSvg){
		$("#flowimg").attr("src", 
				basePath + "/diagram-viewer/index.html?processDefinitionId=" + processDefinitionId 
				+ "&processInstanceId=" + processInstanceId 
				+ "&_date="+new Date().getTime());
	}else{
		$("#flowimg").attr("src", basePath + "processinstance/trace/" + processInstanceId + "?_date="+new Date().getTime());
	}
	$("#biggerimg").modal("show");
//	window.setInterval("reinitIframe()", 200);
}

function hidebiggerFlow(){
	$("#biggerimg").modal("hide");
}

function addRow(){
	var lastrow = $("#paramtable").find("tr").last();
	var rowid = lastrow.attr("id");
	var index = rowid.split("_")[1];
	index++;
	var newrowid = "row_" + index;
	var newrow = lastrow.clone();
	newrow.attr("id", newrowid);
	lastrow.after(newrow);
}

function delRow(){
	var lastrow = $("#paramtable").find("tr").last();
	var rowid = lastrow.attr("id");
	var index = rowid.split("_")[1];
	if(index == 1){
		alert("只有一个参数了,不能删除了!");
		return;
	}
	$("#paramtable").find("tr").last().remove();
}

function pass(processInstanceId) {
	var rightDiv = $("#right_div");
	var json = {
			"taskId" : rightDiv.find("[name=taskId]").val()
		};
	$("#paramtable").find("tr").each(function(i, e){
		if(e.id != "row_0"){
			var key = $(e).find("[name='key']").val();
			var value = $(e).find("[name='value']").val();
			var data = {};
			json[key] = value;
		}
	});
	$.ajax({
		url : basePath + "task/complete/" + processInstanceId,
		type: "post",
		data: json,
		success : function(data) {
			alert('passed');
			window.location.href = basePath + data;
			$("#errortext").html("");
		},
		error : function(e){
			$("#errortext").html(e.responseText);
		}
	});
}

function back(processInstanceId) {
	var rightDiv = $("#right_div");
	var taskId = rightDiv.find("[name=taskId]").val();
	if($.isEmptyObject(taskId)){
		$("#warning_div").html("taskId不能为空!");
		$("#warning_div").fadeIn(3000,function(){
			$("#warning_div").fadeOut(3000);
		});
		return;
	}
	var rightDiv = $("#right_div");
	var json = {
			"taskId" : taskId
		};
	$("#paramtable").find("tr").each(function(i, e){
		if(e.id != "row_0"){
			var key = $(e).find("[name='key']").val();
			var value = $(e).find("[name='value']").val();
			var data = {};
			json[key] = value;
		}
	});
	$.ajax({
		url : basePath + "task/back/" + processInstanceId,
		type: "post",
		data: json,
		success : function(data) {
			alert('backed');
			window.location.href = basePath + data;
			$("#errortext").html("");
		},
		error : function(e){
			$("#errortext").html(e.responseText);
		}
	});
}

function backTo(processInstanceId) {
	var rightDiv = $("#right_div");
	var taskId = rightDiv.find("[name=taskId]").val();
	var toTaskId = rightDiv.find("[name=toTaskId]").val();
	if($.isEmptyObject(taskId)){
		$("#warning_div").html("taskId不能为空!");
	}
	if($.isEmptyObject(toTaskId)){
		$("#warning_div").html($("#warning_div").html()+"<br/>toTaskId不能为空!");
	}
	if($.isEmptyObject(taskId) || $.isEmptyObject(toTaskId)){
		$("#warning_div").fadeIn(3000,function(){
			$("#warning_div").fadeOut(3000);
		});
		return;
	}
	var rightDiv = $("#right_div");
	var json = {
			"taskId" : taskId,
			"toTaskId" : toTaskId
		};
	$("#paramtable").find("tr").each(function(i, e){
		if(e.id != "row_0"){
			var key = $(e).find("[name='key']").val();
			var value = $(e).find("[name='value']").val();
			var data = {};
			json[key] = value;
		}
	});
	$.ajax({
		url : basePath + "task/back/" + processInstanceId,
		type: "post",
		data: json,
		success : function(data) {
			alert('backToed');
			window.location.href = basePath + data;
			$("#errortext").html("");
		},
		error : function(e){
			$("#errortext").html(e.responseText);
		}
	});
}
function deployModel(modelId){
	$.ajax({
		url : basePath + "/model/deploy/" + modelId,
		type: "post",
		success : function(data) {
			if(data == "/model/querylist"){
				alert('It is not deployed, please edit again!');
			}else{
				alert('deployed');
			}
			window.location.href = basePath + data;
			$("#errortext").html("");
		},
		error : function(e){
			$("#errortext").html(e.responseText);
		}
	});
}

function deleteModel(modelId){
	$.ajax({
		url : basePath + "/model/" + modelId + "/delete",
		type: "post",
		success : function(data) {
			alert('deleted');
			window.location.href = basePath + data;
			$("#errortext").html("");
		},
		error : function(e){
			$("#errortext").html(e.responseText);
		}
	});
}

function reinitIframe() {
	var iframe = document.getElementById("flowimg");
	try {
		var bHeight = iframe.contentWindow.document.body.scrollHeight;
		var dHeight = iframe.contentWindow.document.documentElement.scrollHeight;
		var height = Math.max(bHeight, dHeight);
		iframe.height = height;
		console.log(height);
	} catch(ex) {}
}
