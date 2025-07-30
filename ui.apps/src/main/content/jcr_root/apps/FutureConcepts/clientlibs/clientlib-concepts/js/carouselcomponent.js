
document.addEventListener('DOMContentLoaded', function () {
    const track = document.querySelector('.carousel-track');
    const slides = Array.from(track.children);
    const prevButton = document.querySelector('.carousel-btn.prev');
    const nextButton = document.querySelector('.carousel-btn.next');
    const pauseButton = document.querySelector('.carousel-btn.pause');

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

    nextButton.addEventListener('click', () => {
      goToNextSlide();
    });

    pauseButton.addEventListener('click', () => {
      if (autoPlay) {
        stopAutoPlay();
      } else {
        startAutoPlay();
      }
    });

    window.addEventListener('resize', updateCarousel);

    updateCarousel();
    startAutoPlay();
});