let checkout_form = $("#checkout_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleCheckoutResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle checkout response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] == "success") {
        alert("Success!");
        window.location.replace("confirm");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "checkout_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        alert("Invalid credentials!")
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitCheckoutForm(formSubmitEvent) {
    console.log("submit checkout form");
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/checkout", {
            method: "POST",
            // Serialize the checkout form to the data sent by POST request
            data: checkout_form.serialize(),
            success: handleCheckoutResult
        }
    );
    console.log("masuk!");
}

// Bind the submit action of the form to a handler function
checkout_form.submit(submitCheckoutForm);