var 
  select = function(s) {
    return document.querySelector(s);
  },
  selectAll = function(s) {
    return document.querySelectorAll(s);
  }, 
    mark = select('.mark'),
    num = 18,
    step = 360/num,
    container1 = select('.container1'),
    container2 = select('.container2'),
    mainTl,
    count = 0
  

TweenMax.set('svg', {
  visibility: 'visible'
})


mainTl = new TimelineMax({});



function makeDial(container, radius, alpha){
 var angle, clone, point, tl, cloneLabel;
 
 for(var i = 0; i < num; i++){
  angle = step * i;
  clone = mark.cloneNode(true);
  container.appendChild(clone);
  point = {
    x: (Math.cos(angle * Math.PI / 180) * radius) + 400,
    y: (Math.sin(angle * Math.PI / 180) * radius) + 300
  } 
  TweenMax.set(clone, {
   x:point.x,
   y:point.y,
   rotation: angle,
   alpha:alpha
  })
  
  tl = new TimelineMax({repeat:-1});
  tl.to(clone, 1, {
   attr:{
    x2:60
   },
   ease:Power3.easeInOut
  })
.to(clone, 2, {
   attr:{
    x1:80,
    x2:80
   },
   ease:Power1.easeInOut
  })

  mainTl.add(tl, count/10)
  count++;
 }

}

makeDial(container1, 33, 0.3)
makeDial(container2, 33, 1)

TweenMax.set([container2, container1], {
 transformOrigin:'50% 50%'
})


mainTl.timeScale(2)