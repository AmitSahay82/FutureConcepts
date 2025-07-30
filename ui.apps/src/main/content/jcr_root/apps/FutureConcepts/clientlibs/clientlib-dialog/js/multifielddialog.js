(function () {
  const MF_SELECTOR = 'coral-multifield[data-granite-coral-multifield-name="./article"]';

  $(document).on("dialog-ready", function () {
    const multifield = document.querySelector(MF_SELECTOR);
    if (!multifield) {
      console.warn("❌ Multifield './article' not found");
      return;
    }

    console.log("✅ Dialog ready");

    const addButton = multifield.querySelector('button[coral-multifield-add]');

    // Move Add button to top only once
    if (addButton && addButton !== multifield.firstElementChild) {
      multifield.insertBefore(addButton, multifield.firstChild);
      console.log("⬆️ Add button moved to top");
    }

    // Track added item manually, only once per add
    let lastAddedItem = null;

    multifield.addEventListener("coral-collection:add", function (e) {
      const newItem = e.detail.item;

      // Skip if already handled
      if (lastAddedItem === newItem) return;
      lastAddedItem = newItem;

      // Wait till it's rendered inside DOM
      requestAnimationFrame(() => {
        const addBtn = multifield.querySelector('button[coral-multifield-add]');
        if (addBtn && newItem && addBtn.parentNode === newItem.parentNode) {
          try {
            // Insert new item right after the Add button
            addBtn.parentNode.insertBefore(newItem, addBtn.nextElementSibling);
            console.log("🆕 New item inserted after Add button");

            // Focus first input
            const input = newItem.querySelector("input, textarea, [tabindex]:not([tabindex='-1'])");
            if (input) input.focus();
          } catch (err) {
            console.error("❌ insertBefore failed:", err);
          }
        } else {
          console.warn("⚠️ Unable to insert — parent mismatch or item missing");
        }
      });
    });
  });
})();
