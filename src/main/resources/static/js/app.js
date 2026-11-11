(() => {
  const latinDigits = "0123456789";
  const persianDigits = "۰۱۲۳۴۵۶۷۸۹";

  function toPersian(value) {
    return String(value).replace(/[0-9]/g, (d) => persianDigits[latinDigits.indexOf(d)]);
  }

  async function postJson(url, body) {
    const res = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const problem = await res.json().catch(() => ({}));
      throw new Error(problem.detail || "خطا در محاسبه");
    }
    return res.json();
  }

  const calcForm = document.getElementById("calc-form");
  if (calcForm) {
    const out = document.getElementById("calc-result");
    const modeSelect = document.getElementById("calc-mode");
    const gramsField = document.getElementById("grams-field");
    const portionField = document.getElementById("portion-field");

    function syncMode() {
      const mode = modeSelect.value;
      gramsField.hidden = mode !== "grams";
      portionField.hidden = mode !== "portion";
    }
    modeSelect.addEventListener("change", syncMode);
    syncMode();

    calcForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const foodId = Number(document.getElementById("foodId").value);
      const mode = modeSelect.value;
      const payload = { foodId };
      if (mode === "grams") {
        payload.grams = document.getElementById("grams").value;
      } else {
        payload.portionId = Number(document.getElementById("portionId").value);
        payload.count = document.getElementById("count").value;
      }
      out.textContent = "در حال محاسبه…";
      try {
        const data = await postJson("/api/calc", payload);
        out.innerHTML = `
          <div class="result">${toPersian(data.kcal)} کیلوکالری</div>
          <div class="muted">پروتئین ${toPersian(data.protein)} · کربوهیدرات ${toPersian(data.carb)} · چربی ${toPersian(data.fat)} · گرم ${toPersian(data.grams)}</div>
        `;
      } catch (err) {
        out.textContent = err.message;
      }
    });
  }

  const mealForm = document.getElementById("meal-form");
  if (mealForm) {
    mealForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const status = document.getElementById("meal-status");
      status.textContent = "در حال ثبت…";
      try {
        const mealRes = await postJson("/api/meals", {
          name: document.getElementById("mealName").value,
          eatenDate: document.getElementById("eatenDate").value,
          eatenTime: document.getElementById("eatenTime").value,
        });
        const foodId = Number(document.getElementById("mealFoodId").value);
        const grams = document.getElementById("mealGrams").value;
        await postJson(`/api/meals/${mealRes.id}/entries`, { foodId, grams });
        status.textContent = "ثبت شد. در حال تازه‌سازی…";
        window.location.reload();
      } catch (err) {
        status.textContent = err.message;
      }
    });
  }
})();
