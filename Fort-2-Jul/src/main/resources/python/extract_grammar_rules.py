from bs4 import BeautifulSoup
import requests
import json
import re


def fetch_html(url):
    response = requests.get(url)

    if response.status_code != 200:
        print("ERROR -> Failed to fetch URL: " + url)
        return None

    return response.text


def extract_rules(content):
    soup = BeautifulSoup(content, 'html.parser')

    grammar_rules = {}

    pre_tags = soup.find_all('pre')

    i = 0
    for tag in pre_tags:
        i += 1

        parts = tag.text.split('::=')

        rule_name = parts[0].strip()
        productions = parts[1].strip().split('\r\n\t')

        for production in productions:
            items = []
            group_item = ''
            in_group = False

            for item in production.strip().split(' '):
                if item.startswith('{'):
                    in_group = True
                    group_item += item + ' '
                elif item.endswith('}*'):
                    in_group = False
                    group_item += item
                    items.append(group_item)
                    group_item = ''
                elif in_group:
                    group_item += item + ' '
                else:
                    items.append(item)

            if rule_name in grammar_rules:
                grammar_rules[rule_name].append({"production": items})
            else:
                grammar_rules[rule_name] = [{"production": items}]

    rules = {}

    for rule_name, productions in grammar_rules.items():
        if rule_name == 'terminal_symbols':
            continue

        rule_productions = []

        for production in productions:
            rule_productions.append(production)

        rules[rule_name] = rule_productions

    # Extract terminal symbols
    terminal_symbols = []
    bottom_rules = []  # New list for bottom rules
    li_tags = soup.find_all('li')
    keywords = []

    start = False

    for li in li_tags:
        if "terminal symbols:" in li.text:
            for span in li.find_all('span', class_='t'):
                terminal_symbols.append(span.text)
        if "bottom" in li.text:
            for code in li.find_all('code', class_='nt'):
                bottom_rules.append(code.text)
        if "keywords (" in li.text:
            start = True
        if start:
            for span in li.find_all('span'):  # Extract text from span tags within the list
                if "), " in span.text:
                    start = False  # Stop collecting keywords
                    break
                keywords.append(span.text)


    grammar_rules['terminal_symbols'] = terminal_symbols

    if i != 432:
        print("ERROR -> Expected 432 rules but found " + str(i) + " rules instead")

    json_output = {
        "terminal_symbols": grammar_rules['terminal_symbols'],
        "rules": rules,
        "bottom_rules": bottom_rules,  # Add bottom rules to the output
        "keywords": keywords  # Add keywords to the output
    }

    return json_output


def write_to_json(grammar_rules, file_path):
    with open(file_path + 'grammar_rules.json', 'w') as file:
        json.dump(grammar_rules, file, indent=4)


if __name__ == '__main__':
    url = 'https://slebok.github.io/zoo/fortran/f90/waite-cordy/extracted/index.html#NamedConstantUse'
    content = fetch_html(url)
    grammar_rules = extract_rules(content)
    write_to_json(grammar_rules, '/users/wal/Documents/GitHub/PartIII-Project/Fort-2-Jul//src/main/resources/json/')