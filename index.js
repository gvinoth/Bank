
function redirectToSendMoney() {
    window.location.href = 'transaction.html';
}

function redirectToBalance() {
    window.location.href = 'balance.html';
}

function redirectToStatement() {
    window.location.href = 'statement.html';
}

function redirectToAddCustomer() {
    window.location.href = 'addCustomer.html';
}

function redirectToEditCustomer() {
    window.location.href = 'editCustomer.html';
}

function redirectToRemoveCustomer() {
    window.location.href = 'removeCustomer.html';
}

function redirectToAddNominee() {
    window.location.href = 'addNominee.html';
}

function redirectToEditNominee() {
    window.location.href = 'editNominee.html';
}

function redirectToRemoveNominee() {
    window.location.href = 'removeNominee.html';
}

function redirectToCard() {
    window.location.href = 'card.html';
}

function redirectToBlock() {
    window.location.href = 'block.html';
}

function redirectToInterest() {
    window.location.href = 'interest.html';
}

function redirectToAadhar() {
    window.location.href = 'aadhar.html';
}

function redirectToAddDetails() {
    window.location.href = 'addDetails.html';
}

function redirectToLogDetails() {
    window.location.href = 'logDetails.html';
}

function updateTextBox(option) {
    var selectedOption = option.textContent;
    var textBox = document.querySelector('.dropdown-input');
    textBox.value = selectedOption;
  }

  function validateLogin() {
    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;

    // Check if username and password are not empty
    if (username.trim() === '' || password.trim() === '') {
        alert('Please enter both username and password.');
        return false;
    }
    // Check if username is 'customer' and password is 'password'
    if (username === 'customer' && password === 'password') {
        window.location.href = 'customer.html';
        localStorage.setItem('role', username);
        alert('Login successful!');
    }
    else if (username === 'manager' && password === 'password') {
        localStorage.setItem('role', username);
        window.location.href = 'manager.html';
      
        alert('Login successful!');
    }
    else {
        alert('Invalid username or password.');
    }
}



function checkCondition() {
    var condition = document.getElementById('acc').value;
    if (condition.trim() === '') {
        alert('Please enter both username and password.');
        return false;
    }

    if (condition === 'AC0001') {
        document.getElementById("result").innerHTML = '<div class="btn"><button onclick="sendMail()">Send Mail</button></div>';
    } else {
        document.getElementById("result").innerHTML = '<p>Updated</p>';
    }
}

function sendMail() {
    // Functionality to send mail
    alert("Mail sent successfully!");
}

