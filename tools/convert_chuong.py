# -*- coding: utf-8 -*-
"""Chuyển 1 file docx chương thành câu hỏi rồi GỘP vào questions.json.

Cách dùng: python convert_chuong.py <file.docx> <so_chuong> [--dap-an SO=CHI_SO ...]

Hỗ trợ 2 kiểu đánh dấu đáp án trong docx:
  1. Ký hiệu ô chọn: ☑ = đúng, ☐ = sai (mỗi dòng một đáp án).
  2. Bôi highlight đáp án đúng (không có ô chọn) — dạng của phần biển báo:
     - nhiều dòng: mỗi dòng một lựa chọn, dòng bôi màu là đúng;
     - một dòng:   các lựa chọn ngăn cách ". ", cụm bôi màu là đúng.

--dap-an 364=1 435=3 : bổ sung đáp án đúng (chỉ số 1-based) cho các câu mà
file gốc quên đánh dấu. Đáp án do NGƯỜI DÙNG cung cấp, script không tự đoán.
Idempotent: chạy lại sẽ thay thế toàn bộ câu của chương đó."""
import zipfile, re, os, json, sys

DOCX, CHUONG = sys.argv[1], int(sys.argv[2])
OVERRIDES = {}
for arg in sys.argv[3:]:
    m = re.match(r'^(\d+)=(\d+)$', arg)
    if m:
        OVERRIDES[int(m.group(1))] = int(m.group(2))
PREFIX = f'c{CHUONG}'
ASSETS_IMG = 'app/src/main/assets/images'
OUT_JSON = 'app/src/main/assets/questions.json'

CHECKED = {'☑', '☒'}
BOX = {'☐', '☒', '☑', '□', '■'}
STAR = {'★', '⭐'}

z = zipfile.ZipFile(DOCX)
xml = z.read('word/document.xml').decode('utf-8')
rels = z.read('word/_rels/document.xml.rels').decode('utf-8')
rid2media = {m.group(1): os.path.basename(m.group(2))
             for m in re.finditer(r'Id="(rId\d+)"[^>]*Target="([^"]+)"', rels)
             if 'media/' in m.group(2)}

def giai_ma(t):
    return (t.replace('&amp;','&').replace('&lt;','<').replace('&gt;','>')
             .replace('&quot;','"').replace('&apos;',"'"))

# Chú ý (?:\s[^>]*)?: KHÔNG được dùng [^>]* trần — nó khớp nhầm cả <w:tab>, <w:tbl>...
WT = re.compile(r'<w:t(?:\s[^>]*)?>(.*?)</w:t>', re.S)

# Các cách file docx của nhóm đánh dấu đáp án đúng bằng ĐỊNH DẠNG chữ:
# bôi highlight (chương 6) hoặc tô màu đỏ / xanh lá (chương 7).
MAU_DAP_AN = re.compile(r'<w:color w:val="(?:EE0000|FF0000|C00000|00B050)"')

def para_text(p):
    return giai_ma(''.join(WT.findall(p)))

def para_runs(p):
    """Danh sách (đoạn chữ, được_đánh_dấu) của một paragraph.
    Đánh dấu = highlight HOẶC màu chữ đỏ/xanh — tuỳ file dùng kiểu nào."""
    out = []
    for run in re.findall(r'<w:r\b.*?</w:r>', p, re.S):
        rt = giai_ma(''.join(WT.findall(run)))
        if rt:
            marked = '<w:highlight' in run or bool(MAU_DAP_AN.search(run))
            out.append((rt, marked))
    return out

questions, cur = [], None
for p in re.split(r'</w:p>', xml):
    txt = para_text(p).strip()
    m_rid = re.search(r'r:embed="(rId\d+)"', p)
    rid = m_rid.group(1) if m_rid else None
    mcau = re.match(r'^Câu\s+(\d+)\s*/\s*(.*)$', txt, re.S)
    if mcau:
        if cur: questions.append(cur)
        content = mcau.group(2).strip()
        diem_liet = False
        for s in STAR:
            if content.startswith(s):
                diem_liet = True; content = content[1:].strip()
        cur = {'so': int(mcau.group(1)), 'noi_dung': content,
               'diem_liet': diem_liet, 'dap_an': [], 'media': None, 'hl_cands': []}
        # Ảnh có thể nằm ngay trong đoạn tiêu đề câu hỏi — không được bỏ qua.
        if rid and rid in rid2media:
            cur['media'] = rid2media[rid]
        continue
    if cur is None: continue
    if rid and rid in rid2media and cur['media'] is None:
        cur['media'] = rid2media[rid]
    if txt and txt[0] in BOX:
        # ☑ là đúng; hoặc ô chọn kết hợp tô màu (một số câu sa hình dùng ☐ + màu).
        checked = txt[0] in CHECKED or any(hl for _, hl in para_runs(p))
        ans = txt[1:].strip()
        mnum = re.match(r'^(\d+)\s*[.)]\s*(.*)$', ans)
        num = int(mnum.group(1)) if mnum else None
        ans_text = mnum.group(2).strip() if mnum else ans
        if ans_text or num:
            cur['dap_an'].append({'num': num, 'text': ans_text, 'checked': checked})
    elif txt:
        # Không có ô chọn — ứng viên cho dạng đáp án đánh dấu bằng định dạng chữ.
        cur['hl_cands'].append((txt, para_runs(p)))
if cur: questions.append(cur)

# Một số file (sa hình) để nội dung câu hỏi ở đoạn văn RIÊNG ngay sau dòng "Câu N/".
# Khi đó ứng viên đầu tiên chính là nội dung câu hỏi, không phải một lựa chọn.
for q in questions:
    if not q['noi_dung'] and q['hl_cands']:
        text, rr = q['hl_cands'].pop(0)
        if any(hl for _, hl in rr):
            # Đoạn đầu chứa cả nội dung câu hỏi LẪN đáp án được tô màu
            # (VD "Xe nào vượt đúng...? Cả 2 xe đều đúng."). Cắt tại run
            # được đánh dấu đầu tiên: trước là câu hỏi, từ đó là lựa chọn 1.
            i0 = next(i for i, (_, hl) in enumerate(rr) if hl)
            q['noi_dung'] = ''.join(rt for rt, _ in rr[:i0]).strip()
            opt = ''.join(rt for rt, _ in rr[i0:]).strip()
            q['hl_cands'].insert(0, (opt, [(opt, True)]))
        else:
            q['noi_dung'] = text

def tach_dong_don(cands):
    """Một dòng chứa nhiều lựa chọn ngăn cách '. ' — cụm bôi màu là đáp án đúng.
    Ghép run thành chuỗi đầy đủ, ghi lại các khoảng ký tự được bôi màu, rồi chọn
    lựa chọn TRÙNG NHIỀU NHẤT với vùng bôi màu (run có thể cắt giữa chữ)."""
    text_full, hl_ranges, pos = '', [], 0
    for rt, hl in cands[0][1]:
        if hl:
            hl_ranges.append((pos, pos + len(rt)))
        text_full += rt
        pos += len(rt)
    out, best_i, best_overlap = [], -1, 0
    for m in re.finditer(r'[^.]+(?:\.|$)', text_full):
        opt = m.group(0).strip()
        if not opt:
            continue
        overlap = sum(max(0, min(m.end(), b) - max(m.start(), a)) for a, b in hl_ranges)
        out.append({'num': None, 'text': opt, 'checked': False})
        if overlap > best_overlap:
            best_overlap, best_i = overlap, len(out) - 1
    if best_i >= 0:
        out[best_i]['checked'] = True
    return out

# Câu không có đáp án dạng ô chọn -> thử dạng highlight.
for q in questions:
    if q['dap_an'] or not q['hl_cands']:
        continue
    cands = [c for c in q['hl_cands'] if c[0]]
    if len(cands) >= 2:
        # Mỗi dòng một lựa chọn; dòng có run bôi màu là đáp án đúng.
        q['dap_an'] = [{'num': None, 'text': t,
                        'checked': any(hl for _, hl in rr)} for t, rr in cands]
    elif len(cands) == 1:
        q['dap_an'] = tach_dong_don(cands)

# Dạng "chọn Hình N": chỉ 1 dòng đáp án (đúng) + có ảnh -> sinh đủ lựa chọn Hình 1..n
fixed_hinh = []
for q in questions:
    if len(q['dap_an']) == 1 and q['media'] and q['dap_an'][0]['checked']:
        dungnum = q['dap_an'][0]['num'] or 1
        n = max(3, dungnum)
        q['dap_an'] = [{'num': i, 'text': f'Hình {i}', 'checked': i == dungnum}
                       for i in range(1, n+1)]
        fixed_hinh.append(q['so'])

for q in questions:
    texts, dung = [], -1
    for i, a in enumerate(q['dap_an']):
        if a['checked']: dung = i
        texts.append(a['text'] if a['text'] else f"Hình {a['num']}")
    q['dap_an'] = texts
    q['dung'] = dung

# Đáp án bổ sung từ người dùng (--dap-an SO=CHI_SO, 1-based) cho câu file gốc quên đánh dấu.
for q in questions:
    if q['so'] in OVERRIDES:
        chi_so = OVERRIDES[q['so']] - 1
        if 0 <= chi_so < len(q['dap_an']):
            q['dung'] = chi_so
            print(f"OVERRIDE: Câu {q['so']} -> đáp án {OVERRIDES[q['so']]} ({q['dap_an'][chi_so][:50]})")
        else:
            print(f"OVERRIDE LỖI: Câu {q['so']} chỉ có {len(q['dap_an'])} đáp án, không có số {OVERRIDES[q['so']]}")

anomalies = [f"Câu {q['so']}: {len(q['dap_an'])} đáp án" for q in questions if len(q['dap_an']) < 2]
anomalies += [f"Câu {q['so']}: không có đáp án đúng" for q in questions if q['dung'] < 0]

print('FILE:', DOCX, '| CHƯƠNG:', CHUONG)
print('TỔNG SỐ CÂU:', len(questions), '| số hiệu:', questions[0]['so'], '->', questions[-1]['so'])
print('Điểm liệt (★):', sorted(q['so'] for q in questions if q['diem_liet']) or '(không)')
print('Dạng Hình đã sinh lựa chọn:', fixed_hinh or '(không)')
print('Câu có ảnh:', [(q['so'], q['media']) for q in questions if q['media']] or '(không)')
print('ANOMALIES:', anomalies if anomalies else 'KHÔNG')
if anomalies:
    print('>>> DỪNG, không ghi file'); sys.exit(1)

os.makedirs(ASSETS_IMG, exist_ok=True)
for q in questions:
    if q['media']:
        ext = os.path.splitext(q['media'])[1] or '.png'
        name = f"{PREFIX}_{q['so']:03d}{ext}"
        with open(os.path.join(ASSETS_IMG, name), 'wb') as f:
            f.write(z.read('word/media/' + q['media']))
        q['anh'] = 'images/' + name

with open(OUT_JSON, encoding='utf-8') as f:
    data = json.load(f)
data['cau_hoi'] = [c for c in data['cau_hoi'] if c.get('chuong') != CHUONG]
so_truoc = len(data['cau_hoi'])
for q in questions:
    obj = {'chuong': CHUONG, 'noi_dung': q['noi_dung'],
           'diem_liet': q['diem_liet'], 'giai_thich': ''}
    if q.get('anh'): obj['anh'] = q['anh']
    obj['dap_an'] = q['dap_an']
    obj['dap_an_dung'] = q['dung']
    data['cau_hoi'].append(obj)
with open(OUT_JSON, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)
print(f">>> ĐÃ GỘP: {so_truoc} câu cũ + {len(questions)} câu chương {CHUONG} = {len(data['cau_hoi'])} câu")
