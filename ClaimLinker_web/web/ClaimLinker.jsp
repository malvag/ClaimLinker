<!doctype html>
<html lang="en">

    <head>
        <!-- Required meta tags -->
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

        <!-- Bootstrap CSS -->
        <link rel="stylesheet" type="text/css" href="../resource/css/bootstrap.min.css">

        <title>ClaimLinker - Linking text to ClaimsKG</title>
    </head>

    <body>
        <div class="container">
            <div class="jumbotron">
                <h1 class="display-4">ClaimLinker!</h1>
                <p class="lead">Linking text to ClaimsKG - a knowledge base of fact-checked claims (<a href="https://data.gesis.org/claimskg/" target="_blank">about ClaimsKG</a>, <a href="https://data.gesis.org/claimskg/sparql" target="_blank">SPARQL endpoint</a>) </p>
                <hr class="my-4">
                <a class="btn btn-primary btn-lg" href="https://github.com/malvagos/ClaimLinker/" role="button">Learn more</a>

            </div>
            <div class="jumbotron">
                <div class="input-group">
                    <textarea class="form-control" id="text" aria-label="With textarea">You know, interest on debt will soon exceed security spending.</textarea>
                </div>
                <div class="alert alert-info" role="alert" style="margin-top: 20px">
                    You can also use the ClaimLinker service as you browse the Web through the following bookmarklet:
                    <a href="javascript:window.open('http://139.91.183.92/claimlinker/viaBookmarklet?url='+encodeURI(window.location.toString())+'&text='+(window.getSelection()))"><b>ClaimLinker!</b></a>
                    <br />
                    How to use it:
                    <ol>
                        <li>Drag and drop the above link in your bookmarks</li>
                        <li>Select a piece of text in a webpage and click the bookmark</li>
                    </ol>
                </div>
                <hr class="my-4">
                <button type="submit" id="annotate_btn" onclick="annotate()" class="btn btn-primary">Submit</button>
            </div>
        </div>
        <div class="container" id="app">
            <center><img src="loading.gif" width="80px" id="loadingImg" style="display:none"/></center>
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
                        if (select.trim() === "") {
                            alert("Please give some text! ");
                            document.getElementById("loadingImg").style.display = "none";
                            return;
                        }

                        document.getElementById("app").innerHTML = "";
                        document.getElementById("app").innerHTML = '<center><img src="loading.gif" width="80px" id="loadingImg" style="display:none"/></center>';
                        document.getElementById("loadingImg").style.display = "block";

                        select = select.replace("[", "(").replace("]", ")");

                        requestObj.open("GET", "../claimlinker?app=demo&text=" + select);
                        requestObj.onreadystatechange = function () {
                            if (requestObj.readyState === 4 && requestObj.status === 200) {
                                // console.log(requestObj.responseText);
                                const obj = JSON.parse(requestObj.responseText);
                                console.log(obj)
                                const cl_results = obj._results
                                // console.log(cl_results)
                                // console.log(cl_results[0])
                                /*
                                 
                                 <ul class="list-group">
                                 <li class="list-group-item">Cras justo odio</li>
                                 <li class="list-group-item">Dapibus ac facilisis in</li>
                                 <li class="list-group-item">Morbi leo risus</li>
                                 <li class="list-group-item">Porta ac consectetur ac</li>
                                 <li class="list-group-item">Vestibulum at eros</li>
                                 </ul>
                                 */
                                document.getElementById("app").innerHTML = '<center><img src="loading.gif" width="80px" id="loadingImg" style="display:none"/></center>';
                                var counter = 0;
                                cl_results.forEach(annotation => {
                                    var html_results = "      <a class=\"list-group-item list-group-item-action active\" data-toggle=\"list\" href=\"#list-home\" role=\"tab\" aria-controls=\"home\">" + (++counter) + ". " + annotation.text + "</a>"
                                    html_results += "<ul class=\"list-group\">"

                                    annotation.linkedClaims.forEach(linked_claim => {
                                        html_results += "<li class=\"list-group-item d-flex align-items-center\">"; //justify-content-between 
                                        var queryLink = "https://data.gesis.org/claimskg/sparql?query=SELECT+*+WHERE+%7B+%7B+%3Fsubject+%3Fpredicate+%3Fobject+FILTER%28%3Fsubject+%3D+%3C" + linked_claim.claim_uri + "%3E%29+%7D+UNION+%7B+%3Fsubject+%3Fpredicate+%3Fobject+FILTER%28%3Fobject+%3D+%3C" + linked_claim.claim_uri + "%3E%29+%7D+%7D+";
                                        html_results += "<a href='" + linked_claim.claimReview_url + "' target='_blank'>" + linked_claim.claimReview_claimReviewed.replace('""', '"') + "</a><span class=\"badge badge-primary badge-pill\" style=\"margin-left: 15px; \">" + linked_claim.rating_alternateName + "</span><span class=\"badge badge-warning badge-pill\" style=\"margin-left: 5px; \"><a href='" + queryLink + "' target='_blank' style=''>ClaimsKG</a>";
                                        html_results += "</li>"
                                    })
                                    html_results += "</ul>"
                                    document.getElementById("app").innerHTML += html_results;
                                });
                                if (counter === 0) {
                                    alert("No results! Please try a different input!");
                                }

                            }
                        }
                        requestObj.send(null);
                    }
        </script>
        <br />&nbsp;<br />
    </body>

</html>