(function ($, Coral) {
  $(document).on("dialog-ready", function () {
    $(".coral-Multifield").each(function () {
      const $multifield = $(this);
      const multifieldEl = this;

      // Move Add button above the list
      const $addButton = $multifield.find("button[coral-multifield-add]");
      const $list = $multifield.find(".coral-Multifield-list");

      if ($addButton.length && $list.length) {
        $addButton.detach().insertBefore($list);
      }

      // Add new items at the top
      multifieldEl.addEventListener("coral-collection:add", function (e) {
        const newItem = e.detail.item;
        $list[0].insertBefore(newItem, $list[0].firstChild);
      });
    });
  });
})(jQuery, Coral);