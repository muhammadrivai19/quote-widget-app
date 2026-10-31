"""
Membuat Laporan_Analisis_KPI_Penjualan_Q1.xlsx
Sheet 1 : Detail_Penjualan  -> data transaksi + kolom kalkulasi + baris TOTAL
Sheet 2 : Summary_Cabang    -> rekap per cabang memakai SUMIF / AVERAGEIF
"""

import csv
import io

from openpyxl import Workbook
from openpyxl.formatting.rule import FormulaRule
from openpyxl.styles import Alignment, Border, Font, PatternFill, Side
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.dimensions import ColumnDimension

# ----------------------------------------------------------------------------
# 1. Data mentah
# ----------------------------------------------------------------------------
RAW_CSV = """ID_Karyawan,Nama_Sales,Cabang,Divisi,Target_Penjualan,Realisasi_Penjualan
EMP-001,Budi Santoso,Jakarta,Elektronik,150000000,165000000
EMP-002,Siti Aminah,Jakarta,Furnitur,120000000,110000000
EMP-003,Andi Wijaya,Surabaya,Elektronik,100000000,75000000
EMP-004,Dewi Lestari,Surabaya,Furnitur,90000000,95000000
EMP-005,Rian Hidayat,Bandung,Elektronik,110000000,115000000
EMP-006,Maya Putri,Bandung,Furnitur,85000000,60000000
EMP-007,Fajar Pratama,Jakarta,Elektronik,130000000,140000000
EMP-008,Nina Marlina,Surabaya,Elektronik,95000000,98000000
EMP-009,Eko Prasetyo,Bandung,Elektronik,105000000,82000000
EMP-010,Rina Kartika,Jakarta,Furnitur,115000000,125000000
EMP-011,Doni Setiawan,Surabaya,Furnitur,80000000,79000000
EMP-012,Tari Anggraini,Bandung,Furnitur,90000000,92000000"""

# --- Pembersihan data -------------------------------------------------------
rows = []
reader = csv.DictReader(io.StringIO(RAW_CSV.strip()))
for rec in reader:
    clean = {}
    for key, value in rec.items():
        key = (key or "").strip()
        value = (value or "").strip()
        if key in ("Target_Penjualan", "Realisasi_Penjualan"):
            clean[key] = int(float(value)) if value else 0          # teks -> angka
        elif key == "ID_Karyawan":
            clean[key] = value.upper()                              # normalisasi ID
        else:
            clean[key] = value.title() if key != "Divisi" else value.title()
    if clean.get("ID_Karyawan") and clean.get("Nama_Sales"):
        rows.append(clean)

rows.sort(key=lambda r: (r["Cabang"], r["ID_Karyawan"]))

# ----------------------------------------------------------------------------
# 2. Style / konstanta
# ----------------------------------------------------------------------------
NAVY = "1F3864"
NAVY_SOFT = "D9E1F2"
GREEN_FILL = PatternFill("solid", start_color="C6EFCE")
RED_FILL = PatternFill("solid", start_color="FFC7CE")
TOTAL_FILL = PatternFill("solid", start_color="BDD7EE")

HEADER_FONT = Font(name="Calibri", size=11, bold=True, color="FFFFFF")
HEADER_FILL = PatternFill("solid", start_color=NAVY)
TOTAL_FONT = Font(name="Calibri", size=11, bold=True, color=NAVY)

THIN = Side(style="thin", color="8EA9DB")
BORDER = Border(left=THIN, right=THIN, top=THIN, bottom=THIN)

CENTER = Alignment(horizontal="center", vertical="center", wrap_text=True)
LEFT = Alignment(horizontal="left", vertical="center")
RIGHT = Alignment(horizontal="right", vertical="center")

FMT_RUPIAH = '"Rp" #,##0'
FMT_PERCENT = "0.0%"

wb = Workbook()

# ----------------------------------------------------------------------------
# 3. Sheet 1 : Detail_Penjualan
# ----------------------------------------------------------------------------
ws = wb.active
ws.title = "Detail_Penjualan"

HEADERS = [
    "ID_Karyawan",
    "Nama_Sales",
    "Cabang",
    "Divisi",
    "Target_Penjualan",
    "Realisasi_Penjualan",
    "% Pencapaian",
    "Bonus_Insentif",
    "Status_Performa",
]

ws.append(HEADERS)
for cell in ws[1]:
    cell.font = HEADER_FONT
    cell.fill = HEADER_FILL
    cell.alignment = CENTER
    cell.border = BORDER
ws.row_dimensions[1].height = 32

first_data_row = 2
last_data_row = first_data_row + len(rows) - 1

for i, rec in enumerate(rows):
    r = first_data_row + i
    ws.cell(row=r, column=1, value=rec["ID_Karyawan"])
    ws.cell(row=r, column=2, value=rec["Nama_Sales"])
    ws.cell(row=r, column=3, value=rec["Cabang"])
    ws.cell(row=r, column=4, value=rec["Divisi"])
    ws.cell(row=r, column=5, value=rec["Target_Penjualan"])
    ws.cell(row=r, column=6, value=rec["Realisasi_Penjualan"])
    # % Pencapaian
    ws.cell(row=r, column=7, value=f"=IF(E{r}=0,0,F{r}/E{r})")
    # Bonus 5% hanya jika pencapaian >= 100%
    ws.cell(row=r, column=8, value=f"=IF(G{r}>=1,0.05*F{r},0)")
    # Status performa
    ws.cell(
        row=r,
        column=9,
        value=(
            f'=IF(G{r}>=1,"Target Tercapai",'
            f'IF(G{r}>=0.8,"Perlu Peningkatan","Underperformed"))'
        ),
    )

# --- Baris TOTAL ------------------------------------------------------------
total_row = last_data_row + 1
ws.cell(row=total_row, column=1, value="TOTAL")
ws.merge_cells(start_row=total_row, start_column=1, end_row=total_row, end_column=4)
ws.cell(row=total_row, column=5, value=f"=SUM(E{first_data_row}:E{last_data_row})")
ws.cell(row=total_row, column=6, value=f"=SUM(F{first_data_row}:F{last_data_row})")
ws.cell(row=total_row, column=7, value=f"=IF(E{total_row}=0,0,F{total_row}/E{total_row})")
ws.cell(row=total_row, column=8, value=f"=SUM(H{first_data_row}:H{last_data_row})")

# --- Format sel data + TOTAL ------------------------------------------------
for r in range(first_data_row, total_row + 1):
    is_total = r == total_row
    for c in range(1, len(HEADERS) + 1):
        cell = ws.cell(row=r, column=c)
        cell.border = BORDER
        if is_total:
            cell.font = TOTAL_FONT
            cell.fill = TOTAL_FILL
        if c in (1, 3, 4):
            cell.alignment = CENTER
        elif c in (5, 6, 8):
            cell.alignment = RIGHT
            cell.number_format = FMT_RUPIAH
        elif c == 7:
            cell.alignment = CENTER
            cell.number_format = FMT_PERCENT
        elif c == 9:
            cell.alignment = CENTER
        else:
            cell.alignment = LEFT

ws.cell(row=total_row, column=1).alignment = CENTER

# --- Conditional formatting -------------------------------------------------
status_range = f"A{first_data_row}:I{last_data_row}"
ws.conditional_formatting.add(
    status_range,
    FormulaRule(
        formula=[f'$I{first_data_row}="Target Tercapai"'],
        fill=GREEN_FILL,
        stopIfTrue=False,
    ),
)
ws.conditional_formatting.add(
    status_range,
    FormulaRule(
        formula=[f'$I{first_data_row}="Underperformed"'],
        fill=RED_FILL,
        stopIfTrue=False,
    ),
)

ws.freeze_panes = "A2"
ws.auto_filter.ref = f"A1:I{last_data_row}"
ws.sheet_view.showGridLines = False

# ----------------------------------------------------------------------------
# 4. Sheet 2 : Summary_Cabang
# ----------------------------------------------------------------------------
ws2 = wb.create_sheet("Summary_Cabang")

S_HEADERS = [
    "Cabang",
    "Total Target",
    "Total Realisasi",
    "% Rata-rata Pencapaian",
    "Total Bonus",
]
ws2.append(S_HEADERS)
for cell in ws2[1]:
    cell.font = HEADER_FONT
    cell.fill = HEADER_FILL
    cell.alignment = CENTER
    cell.border = BORDER
ws2.row_dimensions[1].height = 32

cabang_list = sorted({rec["Cabang"] for rec in rows})
det = "'Detail_Penjualan'"
rng_cabang = f"{det}!$C${first_data_row}:$C${last_data_row}"

for i, cabang in enumerate(cabang_list):
    r = 2 + i
    ws2.cell(row=r, column=1, value=cabang)
    ws2.cell(row=r, column=2, value=f"=SUMIF({rng_cabang},$A{r},{det}!$E${first_data_row}:$E${last_data_row})")
    ws2.cell(row=r, column=3, value=f"=SUMIF({rng_cabang},$A{r},{det}!$F${first_data_row}:$F${last_data_row})")
    ws2.cell(row=r, column=4, value=f"=AVERAGEIF({rng_cabang},$A{r},{det}!$G${first_data_row}:$G${last_data_row})")
    ws2.cell(row=r, column=5, value=f"=SUMIF({rng_cabang},$A{r},{det}!$H${first_data_row}:$H${last_data_row})")

s_total_row = 2 + len(cabang_list)
ws2.cell(row=s_total_row, column=1, value="TOTAL")
ws2.cell(row=s_total_row, column=2, value=f"=SUM(B2:B{s_total_row - 1})")
ws2.cell(row=s_total_row, column=3, value=f"=SUM(C2:C{s_total_row - 1})")
ws2.cell(row=s_total_row, column=4, value=f"=IF(B{s_total_row}=0,0,C{s_total_row}/B{s_total_row})")
ws2.cell(row=s_total_row, column=5, value=f"=SUM(E2:E{s_total_row - 1})")

for r in range(2, s_total_row + 1):
    is_total = r == s_total_row
    for c in range(1, 6):
        cell = ws2.cell(row=r, column=c)
        cell.border = BORDER
        if is_total:
            cell.font = TOTAL_FONT
            cell.fill = TOTAL_FILL
        if c == 1:
            cell.alignment = CENTER if is_total else LEFT
        elif c == 4:
            cell.alignment = CENTER
            cell.number_format = FMT_PERCENT
        else:
            cell.alignment = RIGHT
            cell.number_format = FMT_RUPIAH

ws2.freeze_panes = "A2"
ws2.sheet_view.showGridLines = False

# ----------------------------------------------------------------------------
# 5. Auto-width (menghindari tampilan ###)
# ----------------------------------------------------------------------------
def autofit(sheet, header_row=1, extra=2, min_width=10, max_width=30, samples=None):
    """Lebar kolom = string terpanjang yang akan tampil, bukan teks formula."""
    samples = samples or {}
    for col_idx in range(1, sheet.max_column + 1):
        letter = get_column_letter(col_idx)
        longest = len(str(sheet.cell(row=header_row, column=col_idx).value or ""))
        for row_idx in range(header_row + 1, sheet.max_row + 1):
            val = samples.get((row_idx, col_idx))
            if val is None:
                cell = sheet.cell(row=row_idx, column=col_idx)
                if isinstance(cell.value, str) and cell.value.startswith("="):
                    continue  # jangan pakai panjang formula
                val = "" if cell.value is None else str(cell.value)
            longest = max(longest, len(str(val)))
        width = min(max(longest * 1.1 + extra, min_width), max_width)
        sheet.column_dimensions[letter].width = round(width, 1)


def money(v):
    return f"Rp {v:,.0f}"


def pct(v):
    return f"{v * 100:.1f}%"


# nilai tampilan hasil formula (dipakai hanya untuk mengukur lebar kolom)
samples = {}
for i, rec in enumerate(rows):
    r = first_data_row + i
    p = rec["Realisasi_Penjualan"] / rec["Target_Penjualan"] if rec["Target_Penjualan"] else 0
    bonus = rec["Realisasi_Penjualan"] * 0.05 if p >= 1 else 0
    status = "Target Tercapai" if p >= 1 else ("Perlu Peningkatan" if p >= 0.8 else "Underperformed")
    samples[(r, 7)] = pct(p)
    samples[(r, 8)] = money(bonus)
    samples[(r, 9)] = status

t_target = sum(rec["Target_Penjualan"] for rec in rows)
t_real = sum(rec["Realisasi_Penjualan"] for rec in rows)
t_bonus = sum(rec["Realisasi_Penjualan"] * 0.05 for rec in rows if rec["Realisasi_Penjualan"] >= rec["Target_Penjualan"])
samples[(total_row, 5)] = money(t_target)
samples[(total_row, 6)] = money(t_real)
samples[(total_row, 7)] = pct(t_real / t_target if t_target else 0)
samples[(total_row, 8)] = money(t_bonus)

autofit(ws, samples=samples)
ws.column_dimensions["B"].width = 20          # Nama_Sales lebih lega
ws.column_dimensions["I"].width = 20          # Status_Performa

# Summary_Cabang
s_samples = {}
for i, cabang in enumerate(cabang_list):
    r = 2 + i
    grp = [rec for rec in rows if rec["Cabang"] == cabang]
    g_target = sum(rec["Target_Penjualan"] for rec in grp)
    g_real = sum(rec["Realisasi_Penjualan"] for rec in grp)
    g_bonus = sum(rec["Realisasi_Penjualan"] * 0.05 for rec in grp if rec["Realisasi_Penjualan"] >= rec["Target_Penjualan"])
    g_pct = sum((rec["Realisasi_Penjualan"] / rec["Target_Penjualan"] if rec["Target_Penjualan"] else 0) for rec in grp) / len(grp)
    s_samples[(r, 2)] = money(g_target)
    s_samples[(r, 3)] = money(g_real)
    s_samples[(r, 4)] = pct(g_pct)
    s_samples[(r, 5)] = money(g_bonus)

s_samples[(s_total_row, 2)] = money(t_target)
s_samples[(s_total_row, 3)] = money(t_real)
s_samples[(s_total_row, 4)] = pct(t_real / t_target if t_target else 0)
s_samples[(s_total_row, 5)] = money(t_bonus)

autofit(ws2, samples=s_samples)
ws2.column_dimensions["A"].width = 16
for letter in ("B", "C", "E"):
    ws2.column_dimensions[letter].width = max(ws2.column_dimensions[letter].width, 18)
ws2.column_dimensions["D"].width = max(ws2.column_dimensions["D"].width, 22)

# ----------------------------------------------------------------------------
# 6. Properti dokumen + simpan
# ----------------------------------------------------------------------------
wb.calculation.fullCalcOnLoad = True          # minta Excel hitung ulang saat dibuka

for sheet in (ws, ws2):
    sheet.sheet_properties.pageSetUpPr.fitToPage = True
    sheet.page_setup.orientation = "landscape"
    sheet.page_setup.fitToWidth = 1
    sheet.page_setup.fitToHeight = 0
    sheet.print_title_rows = "1:1"

wb.properties.title = "Laporan Analisis KPI Penjualan Q1"
wb.properties.creator = "Sales Analytics"
wb.properties.subject = "Rekap performa penjualan per sales dan per cabang"

OUT = "Laporan_Analisis_KPI_Penjualan_Q1.xlsx"
wb.save(OUT)

print("File tersimpan:", OUT)
print(f"Sheet 1: {len(rows)} transaksi, baris TOTAL di baris {total_row}")
print("Sheet 2: cabang ->", ", ".join(cabang_list))
print(f"Total target   : Rp {t_target:,.0f}")
print(f"Total realisasi: Rp {t_real:,.0f}")
print(f"Total bonus    : Rp {t_bonus:,.0f}")
