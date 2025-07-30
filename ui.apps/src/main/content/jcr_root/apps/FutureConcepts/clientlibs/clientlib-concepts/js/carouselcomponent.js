document.addEventListener('DOMContentLoaded', function () {
  const carouselComponents = document.querySelectorAll('.carousel-component');

  carouselComponents.forEach(component => {
    const track = component.querySelector('.carousel-track');
    const slides = Array.from(track.children);
    const prevButton = component.querySelector('.carousel-btn.prev');
    const nextButton = component.querySelector('.carousel-btn.next');
    const pauseButton = component.querySelector('.carousel-btn.pause');

    let currentIndex = 0;
    let autoPlay = true;
    let intervalId;

    function updateCarousel() {
      const slideWidth = slides[0].getBoundingClientRect().width;
      track.style.transform = `translateX(-${currentIndex * slideWidth}px)`;
    }

    function goToNextSlide() {
      currentIndex = (currentIndex + 1) % slides.length;
      updateCarousel();
    }

    function startAutoPlay() {
      intervalId = setInterval(goToNextSlide, 4000);
      pauseButton.textContent = '❚❚';
      autoPlay = true;
    }

    function stopAutoPlay() {
      clearInterval(intervalId);
      pauseButton.textContent = '▶';
      autoPlay = false;
    }

    prevButton.addEventListener('click', () => {
      currentIndex = (currentIndex - 1 + slides.length) % slides.length;
      updateCarousel();
    });

    nextButton.addEventListener('click', goToNextSlide);

    pauseButton.addEventListener('click', () => {
      autoPlay ? stopAutoPlay() : startAutoPlay();
    });

    window.addEventListener('resize', updateCarousel);

    updateCarousel();
    startAutoPlay();
  });
});
