<%-- 
    Document   : licenses
    Created on : 15 oct. 2014, 14:44:38
    Author     : trouvin

/*
Copyright [2014] [Pascal TROUVIN, O4S France]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript" src="js/jquery.min.js"></script>
        <link rel="stylesheet" href="css/bootstrap.min.css">
        <script src="js/bootstrap.min.js"></script>
        
        <script>
            $(function(){
                $('.tabbable a').click(function (e) {
                    e.preventDefault();
                    $(this).tab('show');
                  });
                //Ajax tabs
                $('.nav-tabs a[data-href][data-toggle="tab"]').on('show', function(e) {
                    var $this = $(this);
                    if ($this.data('loaded') != 1)
                    {
                      var firstSlash = $this.attr('data-href').indexOf('/');
                      var url = '';
                      if (firstSlash !== 0)
                        url = window.location.href + '/' + $this.attr('data-href');
                      else
                        url = $this.attr('data-href');
                      //Load the page

                      $($this.attr('href')).load(url, function(data) {
                        $this.data('loaded', 1);
                      });
                    }
                  });

            });

        </script>
    </head>
    <body>
        <h1>Licenses</h1>
        
        
        <div class="tabbable">
          <ul class="nav nav-tabs">
            <li class="active">
              <a href="#sarGrapher" data-href="LICENSE.txt" data-toggle="tab">sarGrapher license</a>
            </li>
            <li>
              <a href="#sarGrapher2" data-href="LICENSE.txt" data-toggle="tab">sarGrapher2 license</a>
            </li>
          </ul>
          <div class="tab-content">
            <div class="tab-pane active" id="sarGrapher">
              <p>
                This content isn't used, but instead replaced with contents of tab1.php.
              </p>
              <p>
                You can put a loader image in here if you want
              </p>
            </div>
            <div class="tab-pane active" id="sarGrapher2">
              <p>
                This content isn't used, but instead replaced with contents of tab1.php.
              </p>
              <p>
                You can put a loader image in here if you want
              </p>
            </div>
          </div>
        </div>
    </body>
</html>
