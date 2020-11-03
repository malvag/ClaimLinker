<!doctype html>
<html lang="en">

<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" type="text/css" href="../resource/css/bootstrap.min.css">

    <title>Hello, world!</title>
</head>

<body>
<div class="container">
    <div class="jumbotron">
        <h1 class="display-4">Hello, world!</h1>
        <p class="lead">This is a simple hero unit, a simple jumbotron-style component for calling extra attention to
            featured content or information.</p>
        <hr class="my-4">
        <p>It uses utility classes for typography and spacing to space content out within the larger container.</p>
        <a class="btn btn-primary btn-lg" href="#" role="button">Learn more</a>
    </div>
    <div class="jumbotron">
        <div class="input-group">
            <textarea class="form-control" id="text" aria-label="With textarea"></textarea>
        </div>
        <hr class="my-4">
        <button type="submit" id="annotate_btn" onclick="annotate()" class="btn btn-primary">Submit</button>
    </div>
</div>
<div class="container" id="app">

</div>
<!-- Optional JavaScript -->
<!-- jQuery first, then Popper.js, then Bootstrap JS -->
<script src="https://code.jquery.com/jquery-3.4.1.slim.min.js"
        integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n"
        crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"
        integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo"
        crossorigin="anonymous"></script>
<script src="../resource/js/bootstrap.min.js"></script>
<script>
    function annotate() {

        var requestObj = new XMLHttpRequest();
        var select = document.getElementById("text").value;
        requestObj.open("GET", "../claimlinker?app=demo&text=" + select);
        requestObj.onreadystatechange = function () {
            if (requestObj.readyState === 4 && requestObj.status === 200) {
                // console.log(requestObj.responseText);
                const obj = JSON.parse(requestObj.responseText);
                console.log(obj)

                const cl_results = JSON.parse(obj._results)
                console.log(cl_results)
                console.log(cl_results.clresults[0])
                /*

                <ul class="list-group">
                  <li class="list-group-item">Cras justo odio</li>
                  <li class="list-group-item">Dapibus ac facilisis in</li>
                  <li class="list-group-item">Morbi leo risus</li>
                  <li class="list-group-item">Porta ac consectetur ac</li>
                  <li class="list-group-item">Vestibulum at eros</li>
                </ul>
                 */
                document.getElementById("app").innerHTML= "";
                cl_results.clresults.forEach(annotation =>{
                    var html_results = "      <a class=\"list-group-item list-group-item-action active\" data-toggle=\"list\" href=\"#list-home\" role=\"tab\" aria-controls=\"home\">"+annotation.text+"</a>"
                    html_results += "<ul class=\"list-group\">"

                    annotation.linkedClaims.forEach(linked_claim=> {
                        html_results += "<li class=\"list-group-item d-flex justify-content-between align-items-center\">"
                        console.log(linked_claim.reviewedBody);
                        html_results+=linked_claim.reviewedBody + "<span class=\"badge badge-primary badge-pill\">"+linked_claim.NLPScore+"</span>"
                        html_results+= "</li>"
                    })
                    html_results += "</ul>"
                    document.getElementById("app").innerHTML += html_results;
                })

            }
        }
        requestObj.send(null);
    }
</script>
</body>

</html>