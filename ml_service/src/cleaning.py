import re

def join_spaced_letters(t):
    # "i d i o t" / "i.d.i.o.t" / "i-d-i-o-t" -> "idiot" (4+ single letters in a row)
    return re.sub(r"\b(?:[a-z][ .\-_]+){3,}[a-z]\b",
                  lambda m: re.sub(r"[ .\-_]+", "", m.group()), t)

def clean_text(t):                                   # toxicity
    t = str(t).lower()
    t = re.sub(r"http\S+|www\.\S+", " ", t)
    t = re.sub(r"\d{1,3}(\.\d{1,3}){3}", " ", t)
    t = re.sub(r"@\w+", " ", t)
    t = join_spaced_letters(t)                       # NEW
    t = re.sub(r"(.)\1{3,}", r"\1\1\1", t)
    return re.sub(r"\s+", " ", t).strip()

def clean_spam(t):                                   # spam
    t = str(t).lower()
    t = re.sub(r"http\S+|www\.\S+", " urltoken ", t)
    t = re.sub(r"\d{5,}", " longnumber ", t)
    return re.sub(r"\s+", " ", t).strip()