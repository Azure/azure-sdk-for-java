window.onload = function () {
  let cookies = document.cookie;
  let cookieValue = cookies.split('=');
  if (cookieValue[1] === 'null' || localStorage.getItem('Theme') === 'null') {
    document.getElementById('retro').setAttribute('disabled', 'false');
  } else if (cookieValue[1] === 'Switch Ultra Theme' ||
      localStorage.getItem('Theme') === 'Switch Ultra Theme') {
    document.getElementById('button').innerText = "Switch Retro Theme";
    document.getElementById('retro').setAttribute('disabled', 'false');

  } else if (cookieValue[1] === 'Switch Retro Theme' ||
      localStorage.getItem('Theme') === 'Switch Retro Theme') {
    if (cookieValue[1] === 'Switch Ultra Theme' ||
        localStorage.getItem('Theme') === 'Switch Ultra Theme') {
      document.getElementById('button').innerText = "Switch Retro Theme";
      document.getElementById('retro').setAttribute('disabled', 'false');

      document.getElementById('button').innerText = "Switch Ultra Theme";
      document.getElementById('retro').removeAttribute('disabled');
      document.getElementById('ultra').setAttribute('disabled', 'false');
      localStorage.setItem('Theme', select);

    } else if (select === 'Switch Ultra Theme') {
      document.getElementById('button').innerText = "Switch Retro Theme";
      document.getElementById('ultra').removeAttribute('disabled');
      document.getElementById('retro').setAttribute('disabled', 'false');
      localStorage.setItem('Theme', select);
    }
  } else if (cookieValue[1] === 'Switch Retro Theme' ||
      localStorage.getItem('Theme') === 'Switch Retro Theme') {
    document.getElementById('button').innerText = "Switch Ultra Theme";
    document.getElementById('ultra').setAttribute('disabled', 'false');
  }
}
document.getElementById('button').onclick = function () {
  let select = document.getElementById('button').innerText;
  if (select === 'Switch Retro Theme') {
    let d = new Date();
    days = 365;
    d.setTime(+d + (days * 86400000)); //24 * 60 * 60 * 1000
    document.cookie = "Theme =" + select + "; expires=" + d.toGMTString() + ";";
    document.getElementById('button').innerText = "Switch Ultra Theme";
    document.getElementById('retro').removeAttribute('disabled');
    document.getElementById('ultra').setAttribute('disabled', 'false');
    localStorage.setItem('Theme', select);

  } else if (select === 'Switch Ultra Theme') {
    let d = new Date();
    days = 365;
    d.setTime(+d + (days * 86400000)); //24 * 60 * 60 * 1000
    document.cookie = "Theme =" + select + "; expires=" + d.toGMTString() + ";";
    document.getElementById('button').innerText = "Switch Retro Theme";
    document.getElementById('ultra').removeAttribute('disabled');
    document.getElementById('retro').setAttribute('disabled', 'false');
    localStorage.setItem('Theme', select);
  }
}
//Function to mouse hovering affect.
document.getElementById('button').onmouseover = function () {
  document.getElementById('button').style.borderRadius = "25px";
  document.getElementById('button').style.width = "180px";
  document.getElementById('button').style.height = "45px";
  document.getElementById('button').style.marginTop = "1px";

}
//Function to mouse out affect
document.getElementById('button').onmouseout = function () {
  document.getElementById('button').style.borderRadius = "25px";
  document.getElementById('button').style.width = "150px";
  document.getElementById('button').style.height = "30px";
  document.getElementById('button').style.marginTop = "8px";

}

//This is the file where we handle the switching of the Themes.
/*Author:- Akhil Gullapalli*/
